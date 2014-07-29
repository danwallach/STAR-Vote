import sys, re

if (len(sys.argv) > 1):
    try:
	    page = open(sys.argv[1]).read()
    except IOError:
        print "Couldn't read the file. Try again!"     
        page = ""
else: 
    page = ""

page = page.replace("missing wiki", "wiki")
page = page.replace("nofollow", "follow")
page = page.replace("?", "")

newPage = ""
last = 0
end = 0
happened = False

for i in [m.start() for m in re.finditer("/wiki/", page)]:
    happened = True    
    newPage += page[last:i]
    end = page.find("\"", i)
    sub = page[i:end]
    sub = sub.replace("/wiki/", "")
    sub += ".html"
    newPage += sub
    last = end

if happened:
    newPage += page[end:] 
else: 
    newPage = page



print newPage
