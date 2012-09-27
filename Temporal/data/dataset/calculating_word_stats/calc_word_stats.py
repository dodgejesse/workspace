import string

f = open("tempeval.dataset.txt")
linecounter = 0
phrases = {}
words = {}
for line in f:
    linecounter = linecounter + 1
    if (linecounter - 2) % 5 == 0:
        line = line[:-2]
        if line in phrases:
            phrases[line] = phrases[line] + 1
        else:
            phrases[line] = 1
        for word in line.split():
            if word in words:
                words[word] = words[word] + 1
            else:
                words[word] = 1
for pair in sorted(words.items(), key=lambda item: item[1]):
    print pair