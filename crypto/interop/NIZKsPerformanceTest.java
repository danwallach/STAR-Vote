package crypto.interop;

import auditorium.Key;
import auditorium.SimpleKeyStore;
import crypto.BallotEncrypter;
import crypto.adder.AdderInteger;
import crypto.adder.PrivateKey;
import crypto.adder.PublicKey;
import crypto.adder.SearchSpaceExhaustedException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;
import supervisor.model.tallier.ChallengeDelayedWithNIZKsTallier;
import votebox.events.AuthorizedToCastWithNIZKsEvent;
import votebox.events.BallotScannedEvent;
import votebox.events.CommitBallotEvent;
import votebox.events.IAnnounceEvent;
import votebox.middle.Properties;
import votebox.middle.ballot.Ballot;
import votebox.middle.ballot.BallotParser;
import votebox.middle.ballot.Card;
import votebox.middle.ballot.SelectableCardElement;
import votebox.middle.driver.*;
import votebox.middle.view.IView;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;


public class NIZKsPerformanceTest {
	class Pair{
		public Object first  = null;
		public Object second = null;
		
		public Pair(){}
		
		public Pair(Object f, Object s){
			first  = f;
			second = s;
		}
	}
	
	public static final String BALLOT_PATH = "crypto"+File.separatorChar+"interop"+File.separatorChar+ "ballot.zip";
	public static final int TRIAL_COUNT = 10;

	public static Ballot _ballot = null;

	private static File _folderPath = null;

	private static  List<byte[]> _seeds = null;
	private static  Key _publicKey = null;
	private static PublicKey _adderPublicKey = null;
    private static PrivateKey _adderPrivateKey = null;
	
	private static List<Pair> _toDecryptWithoutNIZKs = new ArrayList<>();
	private static List<Pair> _toDecryptWithNIZKs = new ArrayList<>();

    SecureRandom r = new SecureRandom();

	protected static void generateRandom(){
		assert _seeds == null;

		_seeds = new ArrayList<>();

		SecureRandom r = new SecureRandom();
		for(int i = 0; i < TRIAL_COUNT; i++){
			byte[] seed = new byte[128];
			r.nextBytes(seed);

			_seeds.add(seed);
		}
	}

	@BeforeClass
	public static void loadBallotBeforeTest() throws Exception{
		if(_seeds == null)
			generateRandom();

		SimpleKeyStore store = new SimpleKeyStore("keys");

		_publicKey = store.loadKey("0");
		_adderPublicKey = (PublicKey)store.loadAdderKey("public");
        _adderPrivateKey = (PrivateKey)store.loadAdderKey("private");

		File tempBallotPath = File.createTempFile("ballot", "path");
		tempBallotPath.delete();
		tempBallotPath = new File(tempBallotPath,"data");
		tempBallotPath.mkdirs();

		Driver.unzip(BALLOT_PATH, tempBallotPath.getAbsolutePath());

		_folderPath = tempBallotPath;

		BallotParser parser = new BallotParser();
		_ballot = parser.getBallot(new GlobalVarsReader(_folderPath.getAbsolutePath()).parse());
		_ballot.setViewAdapter(new IViewAdapter(){
			public boolean deselect(String uid, boolean playSound) throws UnknownUIDException, DeselectionException {
				return true;
			}

			public Properties getProperties() {
				return new Properties();
			}

			public boolean select(String uid) throws UnknownUIDException, SelectionException {
				return true;
			}

            public IView getView() {
                return null;
            }

            public void setView(IView view) {
            }
        });
	}

	@AfterClass
	public static void deleteTemporaryFiles() throws Exception{
		List<File> toDelete = new ArrayList<>();
		toDelete.add(_folderPath);

		while(toDelete.size() > 0){
			File del = toDelete.remove(0);

			if(del == null)
				continue;

			if(del.isFile()){
				if(!del.delete())
					del.deleteOnExit();
				continue;
			}

			File[] child = del.listFiles();

			if(child != null){
				if(child.length == 0){
					if(!del.delete())
						del.deleteOnExit();
					continue;
				}

                Collections.addAll(toDelete, child);
			
				toDelete.add(del);
			}else{
				if(!del.delete())
					del.deleteOnExit();
            }
		}
	}

	@Test
	public void withoutNIZKs() throws Exception{
		System.out.println("withoutNIZKs:");
		List<Card> cards = _ballot.getCards();

		long elapsedTime = 0;

		for(byte[] seed : _seeds){
			System.out.println("Trial #"+_seeds.indexOf(seed));
			SecureRandom rand = new SecureRandom(seed);

			for(Card card : cards)
				for(SelectableCardElement elem : card.getElements())
					elem.deselect();

			for(Card card : cards){
				List<SelectableCardElement> elems = card.getElements();

				SelectableCardElement toSelect = elems.get(rand.nextInt(elems.size()));
				toSelect.select();
			}

			ListExpression exp = _ballot.getCastBallot();

			long encryptStart = System.currentTimeMillis();
			ListExpression toDecrypt = BallotEncrypter.SINGLETON.encrypt(exp, _publicKey);
			long encryptStop = System.currentTimeMillis();

			elapsedTime += (encryptStop - encryptStart);
			System.out.println("\t"+(encryptStop - encryptStart)+" milliseconds");
			
			Pair toAdd = new Pair();
			toAdd.first  = exp;
			toAdd.second = new Pair(toDecrypt, BallotEncrypter.SINGLETON.getRecentRandom());
			
			_toDecryptWithoutNIZKs.add(toAdd);
		}

		System.out.println("Without NIZKs: "+elapsedTime+" milliseconds total");
		System.out.println("Average per ballot: "+(elapsedTime / _seeds.size()));
		
		Assert.assertTrue(true);
	}

	@Test
	public void withNIZKs() throws Exception{
		System.out.println("withNIZKs:");
		List<Card> cards = _ballot.getCards();

		long elapsedTime = 0;

		for(byte[] seed : _seeds){
			System.out.println("Trial #"+_seeds.indexOf(seed));
			SecureRandom rand = new SecureRandom(seed);

			for(Card card : cards)
				for(SelectableCardElement elem : card.getElements())
					elem.deselect();

			for(Card card : cards){
				List<SelectableCardElement> elems = card.getElements();

				SelectableCardElement toSelect = elems.get(rand.nextInt(elems.size()));
				toSelect.select();
			}

			ListExpression exp = _ballot.getCastBallot();
			List<List<String>> groups = _ballot.getRaceGroups();

			long encryptStart = System.currentTimeMillis();
			ListExpression toDecrypt = BallotEncrypter.SINGLETON.encryptWithProof("0", exp, groups, _adderPublicKey, ASExpression.make("nonce"));
			long encryptStop = System.currentTimeMillis();

			elapsedTime += (encryptStop - encryptStart);
			System.out.println("\t"+(encryptStop - encryptStart)+" milliseconds");
			
			Pair toAdd = new Pair();
			toAdd.first  = exp;
			toAdd.second = new Pair(toDecrypt, BallotEncrypter.SINGLETON.getRecentAdderRandom());
			
			_toDecryptWithNIZKs.add(toAdd);
		}

		System.out.println("With NIZKs: "+elapsedTime+" milliseconds total");
		System.out.println("Average per ballot: "+(elapsedTime / _seeds.size()));
		
		Assert.assertTrue(true);
	}
	
	@Test
	public void decryptWithoutNIZKs() throws Exception{
		System.out.println("decrypt withoutNIZKs:");
		long elapsedTime = 0;
		
		for(Pair p : _toDecryptWithoutNIZKs){
			System.out.println("Trial #"+_toDecryptWithoutNIZKs.indexOf(p));
			ListExpression expected  = (ListExpression)p.first;
			ListExpression encrypted = (ListExpression)((Pair)p.second).first;
			ListExpression random    = (ListExpression)((Pair)p.second).second;
			
			long start = System.currentTimeMillis();
			ListExpression decrypted = BallotEncrypter.SINGLETON.decrypt(encrypted, random, _publicKey);
			long stop = System .currentTimeMillis();
			
			Assert.assertEquals("Decrypted isn't what expected", expected, decrypted);
			
			System.out.println("\t"+(stop - start)+" milliseconds");
			
			elapsedTime += (stop - start);
		}
		
		System.out.println("Without NIZKs: "+elapsedTime+" milliseconds total");
		System.out.println("Average per ballot: "+(elapsedTime / _toDecryptWithoutNIZKs.size()));
		
		Assert.assertTrue(true);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void decryptWithNIZKs() throws Exception{
		System.out.println("decrypt withNIZKs:");
		long elapsedTime = 0;
		
		for(Pair p : _toDecryptWithNIZKs){
			System.out.println("Trial #"+_toDecryptWithNIZKs.indexOf(p));
			ListExpression expected  = (ListExpression)p.first;
			ListExpression encrypted = (ListExpression)((Pair)p.second).first;
			List<List<AdderInteger>> random = (List<List<AdderInteger>>)((Pair)p.second).second;

            long start = System.currentTimeMillis();
			ListExpression decrypted = BallotEncrypter.SINGLETON.adderDecrypt(encrypted, random);
			long stop = System .currentTimeMillis();
			
			Assert.assertEquals("Decrypted size does not match expected size", expected.size(), decrypted.size());
			
			Map<String, Integer> tempMap = new HashMap<>();
			
			for(int i = 0; i < expected.size(); i++){
				ListExpression expectedRacePair = (ListExpression)expected.get(i);
				
				tempMap.put(expectedRacePair.get(0).toString(), Integer.parseInt(expectedRacePair.get(1).toString()));
			}
			
			for(int i = 0; i < decrypted.size(); i++){
				ListExpression decryptedRacePair = (ListExpression)decrypted.get(i);
				
				Integer expectedI = tempMap.get(decryptedRacePair.get(0).toString());
				AdderInteger decryptedI = AdderInteger.fromASE(decryptedRacePair.get(1));
				
				Assert.assertEquals(decryptedRacePair.get(0).toString()+" did no match", expectedI, decryptedI.intValue());
			}
			
			System.out.println("\t"+(stop - start)+" milliseconds");
			
			elapsedTime += (stop - start);
		}
		
		System.out.println("With NIZKs: "+elapsedTime+" milliseconds total");
		System.out.println("Average per ballot: "+(elapsedTime / _toDecryptWithNIZKs.size()));
		
		Assert.assertTrue(true);
	}

    @Test
    public void tallyWithNIZKs() throws Exception{

        System.out.println("tallyWithNIZKs:");
        List<Card> cards = _ballot.getCards();

        ChallengeDelayedWithNIZKsTallier tallier = new ChallengeDelayedWithNIZKsTallier(_adderPublicKey, _adderPrivateKey);

        long elapsedTime = 0;

        HashMap<String, ASExpression> map = new HashMap<>();

        SecureRandom r = new SecureRandom();

        AuthorizedToCastWithNIZKsEvent ATCE = new AuthorizedToCastWithNIZKsEvent(0, 0, _ballot.toASExpression(), "3", _ballot.toASExpression().toVerbatim(),
                AdderKeyManipulator.generateFinalPublicKey(_adderPublicKey));

        for(byte[] seed : _seeds){
            System.out.println("Trial #"+_seeds.indexOf(seed));
            SecureRandom rand = new SecureRandom(seed);

            for(Card card : cards)
                for(SelectableCardElement elem : card.getElements())
                    elem.deselect();

            for(Card card : cards){
                if((r.nextInt() % 7) != 0){

                    List<SelectableCardElement> elems = card.getElements();

                    SelectableCardElement toSelect = elems.get(rand.nextInt(elems.size()));
                    toSelect.select();
                }
            }

            ListExpression exp = _ballot.getCastBallot();
            List<List<String>> groups = _ballot.getRaceGroups();

            ListExpression encBallot = BallotEncrypter.SINGLETON.encryptWithProof("0", exp, groups, AdderKeyManipulator.generateFinalPublicKey(_adderPublicKey), ASExpression.make("nonce"));

            ASExpression nonce = StringExpression.makeString(seed);
            String bid = (r.nextInt() + "");
            CommitBallotEvent event = new CommitBallotEvent(0, nonce, encBallot.toVerbatim(), bid, "456");
            IAnnounceEvent event1 = CommitBallotEvent.getMatcher().match(0, event.toSExp());


            map.put(bid, ((CommitBallotEvent) event1).getNonce());

            BallotScannedEvent ballotScannedEvent = new BallotScannedEvent(3, bid);
            IAnnounceEvent bsE = BallotScannedEvent.getMatcher().match(3, ballotScannedEvent.toSExp());


            long encryptStart = System.currentTimeMillis();
            tallier.recordVotes(((CommitBallotEvent)event1).getBallot(), ((CommitBallotEvent)event1).getNonce());
            map.put(((BallotScannedEvent) bsE).getBID(), ((CommitBallotEvent)event1).getNonce());
            if(r.nextInt()%2 == 0){
                tallier.confirmed(((CommitBallotEvent) event1).getNonce());
                map.remove(((BallotScannedEvent) bsE).getBID());
            }

            long encryptStop = System.currentTimeMillis();

            elapsedTime += (encryptStop - encryptStart);
            System.out.println("\t" + (encryptStop - encryptStart) + " milliseconds");

        }

        Map<String, BigInteger> report = null;
        try{
            for(String string : map.keySet())   {
                tallier.confirmed(map.get(string));
                System.out.println(string);
            }

            report = tallier.getReport();

            for(String s : report.keySet()){
                System.out.println(s + " : " + report.get(s));
            }
        }catch (SearchSpaceExhaustedException e){
            System.out.println("ERROR");
            Assert.assertFalse(true);
        }


        Assert.assertTrue(true);

    }
	
	public static void main(String[] args) throws Exception{
		NIZKsPerformanceTest test = new NIZKsPerformanceTest();
		loadBallotBeforeTest();
		test.withoutNIZKs();
		test.withNIZKs();
		test.decryptWithoutNIZKs();
		deleteTemporaryFiles();
	}
}