# Standard definitions
# D DATE "Date: \w\w\w \w\w\w \d+ \d\d:\d\d:\d\d \d\d\d\d [-+]\d\d\d\d"
D DATE "[^\n]*(?=\n|\Z)"
# A status log header RE.  Captures the commit id in its sole group.
D COMMIT_HEAD "commit ([a-f0-9]+)[ \t]*\n(?:Merge:\s+[0-9a-f]{7}\s+[0-9a-f]{7}[ ]*\n)?${DATE}"
# A full log entry.  Captures the entry. Assume logs messages don't contain
# "==="
D COMMIT_LOG "(===[ ]*\ncommit [a-f0-9]+[ ]*\n(?:Merge:\s+[0-9a-f]{7}\s+[0-9a-f]{7}[ ]*\n)?${DATE}[ ]*\n(?:.|\n)*?(?=\Z|\n===))"
# An arbitrary line of text (works even with ?s)
D ARBLINE "[^\n]*(?=\n|\Z)"
# Zero or more arbitrary full lines of text.
D ARBLINES "(?:(?:.|\n)*(?:\n|\Z)|\A|\Z)"

# init
> init
<<<
# commit sth, add and rm sth
+ heeho.txt heeho.txt
> add heeho.txt
<<<
> commit "heeho!"
<<<
# several checkout
+ dark-heeho.txt dark-heeho.txt
> add dark-heeho.txt
<<<
> rm heeho.txt
<<<
> commit "heeho wa mou shin de i ru! It's dark heeho now!"
<<<

# change branch
> branch king
<<<
> checkout king
<<<
+ king-of-heeho.txt king-of-heeho.txt
> add king-of-heeho.txt
<<<
> rm dark-heeho.txt
<<<
* dark-heeho.txt
> commit "HEEHOHO! I AM THE KING OF HEEHO!"
<<<
> log
===
${COMMIT_HEAD}
HEEHOHO! I AM THE KING OF HEEHO!

===
${COMMIT_HEAD}
heeho wa mou shin de i ru! It's dark heeho now!

===
${COMMIT_HEAD}
heeho!

===
${COMMIT_HEAD}
initial commit

<<<*
D UID ${3}
> checkout ${UID} -- heeho.txt
<<<
> add heeho.txt
<<<
> commit "HEEHOHO! heeho IS BACK HEEHO!"
<<<

# several commit and checkout
> checkout master
<<<
+ dark-heeho2.txt dark-heeho.txt
+ heeho.txt poor-heeho.txt
+ king-of-heeho.txt poor-king-of-heeho.txt
> add dark-heeho2.txt
<<<
> add heeho.txt
<<<
> add king-of-heeho.txt
<<<
> commit "more and more dark heeho! heehoheehohee!"
<<<