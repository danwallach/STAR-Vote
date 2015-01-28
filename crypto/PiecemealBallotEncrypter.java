package crypto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import auditorium.Key;
import crypto.adder.AdderPublicKey;
import sexpression.ASExpression;
import sexpression.ListExpression;
import crypto.adder.AdderInteger;

/**
 * Wrapper around BallotEncrypter to allow for concealing
 * as much of the time spent encrypting as possible behind
 * VoteBox's UI actions.
 *
 * @author Montrose
 */
public class PiecemealBallotEncrypter {
	public static PiecemealBallotEncrypter SINGELTON = new PiecemealBallotEncrypter();

	private Map<String, ListExpression> _voteCache   = new HashMap<>();
	private Map<String, ListExpression> _randomCache = new HashMap<>();

	private boolean _adderMode = false;
	private boolean _pureMode  = false;

	private List<Runnable> _pendingActions = new ArrayList<>();
	
	private Thread         _worker = new Thread(){
		public void run(){
			while(true){
				List<Runnable> copy;

				synchronized(_pendingActions){
					if(_pendingActions.size() == 0){
						try {
							_pendingActions.wait();
						} catch (InterruptedException ignored) {}
					}//if

					copy = new ArrayList<>(_pendingActions);
					_pendingActions.clear();
				}

				for(Runnable r : copy)
					r.run();
			}
		}
	};

	private PiecemealBallotEncrypter(){
		_worker.start();
	}

	/**
	 * Updates a piecemeal encryption of a ballot, if we're using Adder style ballots
	 * 
	 * @param id - the id of the Card that contains the race to be updated (the card that defines the raceGroup at least)
	 * @param singleCard - the (id [counter]) pair list to encrypt
	 * @param cardGroup - the grouping to provide a NIZK for (redundant, but helpful for debugging)
	 * @param publicKey - the key to use to encrypt the ballot
	 */
	public void adderUpdate(final String id, final List<ASExpression> singleCard, final List<String> cardGroup, final AdderPublicKey publicKey){
		if(_pureMode)
			throw new RuntimeException("Cannot mix Adder and VoteBox style ballots");

		_adderMode = true;

		Runnable r = new Runnable(){
			public void run(){
				adderUpdateImpl(id, new ListExpression(singleCard), cardGroup, publicKey);
			}
		};

		synchronized(_pendingActions){
			_pendingActions.add(r);
			_pendingActions.notify();
		}
	}

	/**
	 * Actual implementation of  adderUpdate(...)
	 * 
	 * @see PiecemealBallotEncrypter
	 */
	protected void adderUpdateImpl(String id, ListExpression singleCard, List<String> cardGroup, AdderPublicKey publicKey){
		List<List<String>> groups = new ArrayList<>();
		groups.add(cardGroup);

//		_voteCache.put(id, BallotEncrypter.SINGLETON.encryptWithProof(id, singleCard, groups, null, publicKey, ASExpression.make("nonce")));

		List<AdderInteger> randoms = BallotEncrypter.SINGLETON.getRecentAdderRandom().get(0);
		List<ASExpression> randomExps = new ArrayList<>();
		for(AdderInteger r : randoms)
			randomExps.add(r.toASE());


		_randomCache.put(id, new ListExpression(randomExps));
	}

	/**
	 * Updates a piecemeal encryption of a ballot, using old-school VoteBox ballots.
	 * @param id - the id of the Card that contains these elements
	 * @param singleCard - the (id [counter]) pair list to encrypt.
	 * @param publicKey - the key to use to encrypt the ballot.
	 */
	public void update(final String id, final List<ASExpression> singleCard, final Key publicKey){
		if(_adderMode)
			throw new RuntimeException("Cannot mix Adder and VoteBox style ballots");

		_pureMode = true;

		Runnable r = new Runnable(){
			public void run(){
				updateImpl(id, new ListExpression(singleCard), publicKey);
			}
		};

		synchronized(_pendingActions){
			_pendingActions.add(r);
			_pendingActions.notify();
		}
	}

	/**
	 * Actual implementation of  update(...)
	 * 
	 * @see PiecemealBallotEncrypter
	 */
	protected void updateImpl(String id, ListExpression singleCard, Key publicKey){
		_voteCache.put(id, BallotEncrypter.SINGLETON.encrypt(singleCard, publicKey));
		_randomCache.put(id, BallotEncrypter.SINGLETON.getRecentRandom());
	}

	/**
	 * Destroys data in the encrypter.
	 * Call after concluding a voting session (following cast-commit, etc.)
	 */
	public void clear(){
		_voteCache.clear();
		_randomCache.clear();
		_adderMode = false;
		_pureMode = false;
	}

	/**
	 * Blocks until all pending encryption are processed.
	 * 
	 * @return the encrypted ballot, in a form dependent upon which update was called.
	 */
	public ListExpression getEncryptedBallot(){
		final List<ListExpression> retVal = new ArrayList<>();

		Runnable r = new Runnable(){
			public void run(){
				synchronized(retVal){
					retVal.add(getEncryptedBallotImpl());
					retVal.notify();
				}
			}
		};

		synchronized(_pendingActions){
			_pendingActions.add(r);
			_pendingActions.notify();
		}

		synchronized(retVal){
			try {
				retVal.wait();
			} catch (InterruptedException ignored) {}
		}

		return retVal.get(0);
	}

	protected ListExpression getEncryptedBallotImpl(){
		List<ASExpression> subBallots = new ArrayList<>();

		/*
		 * Kind of a trick here.
		 * Normal ballots are of the form ((id [counter]) (id [counter]) ...)
		 * Adder ballots are of the form ((vote (lots of data for multiple counters...)) (vote (lots of data ...)))
		 * This means that with Adder "on", _voteCache contains a bunch of single element lists
		 * This code handles both ballot types without any branching.
		 */
		for(String id : canonicalIdOrder()){
			ListExpression exp = _voteCache.get(id);
			for(int i = 0; i < exp.size(); i++){
				subBallots.add(exp.get(i));
			}
		}

		return new ListExpression(subBallots);
	}

	/**
	 * Provides a canonical ordering of the ids found in _voteCache.
	 * Necessary given the uncertainty of which order the ballot will be traversed in,
	 * to say nothing of concurrency concerns and call orders.
	 * 
	 * @return the order to traverse the _voteCache in while building the final ballot and/or the random list
	 */
	protected List<String> canonicalIdOrder(){
		List<String> ids = new ArrayList<>();
		ids.addAll(_voteCache.keySet());

		Collections.sort(ids, new Comparator<String>(){
			public int compare(String o1, String o2) {
				String id1 = "";
				String id2 = "";

				for(int i = o1.length() - 1; i>=0; i--)
					if(Character.isDigit(o1.charAt(i)))
						id1= o1.charAt(i) + id1;

				for(int i = o2.length() - 1; i>=0; i--)
					if(Character.isDigit(o2.charAt(i)))
						id2= o2.charAt(i) + id2;

				return (new Integer(id1)).compareTo(new Integer(id2));
			}
		});

		return ids;
	}

	/**
	 * @return A ListExpression containing the random values used to encrypt the last ballot cast
	 */
	public ListExpression getRecentRandom(){
		final List<ASExpression> randoms = new ArrayList<>();


		Runnable r = new Runnable(){
			public void run(){
				synchronized(randoms){
					for(String id : canonicalIdOrder()){
						ListExpression exp = _randomCache.get(id);
						for(int i = 0; i < exp.size(); i++){
							randoms.add(exp.get(i));
						}
					}

					randoms.notify();
				}
			}
		};

		synchronized(_pendingActions){
			_pendingActions.add(r);
			_pendingActions.notify();
		}

		synchronized(randoms){
			try {
				randoms.wait();
			} catch (InterruptedException ignored) {}
		}

		return new ListExpression(randoms);
	}

	/**
	 * @return the random values used to encrypt the last ballot cast.
	 */
	public List<List<AdderInteger>> getRecentAdderRandom(){
		final List<List<AdderInteger>> randoms = new ArrayList<>();

		Runnable r = new Runnable(){
			public void run(){
				synchronized(randoms){
					for(String id : canonicalIdOrder()){
						ListExpression exp = _randomCache.get(id);
						List<AdderInteger> subRandom = new ArrayList<>();
						for(int i = 0; i < exp.size(); i++){
							subRandom.add(AdderInteger.fromASE(exp.get(i)));
						}

						randoms.add(subRandom);
					}

					randoms.notify();
				}
			}
		};

		synchronized(_pendingActions){
			_pendingActions.add(r);
			_pendingActions.notify();
		}

		synchronized(randoms){
			try {
				randoms.wait();
			} catch (InterruptedException ignored) {}
		}

		return randoms;
	}
}
