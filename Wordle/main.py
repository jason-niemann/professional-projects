import csv

#CSV to array
file = open("words.csv", "r")
reader = csv.reader(file)
words = []
for row in reader:
    words.append(row[0])


# Is 5 Letters
def hasLength(length):
    if len(word) == length:
        return True
    else:
        return False


# Letter at specified position
def ___atPos(pos, char):
    if not char:
        return True
    if word[pos - 1] == char:
        return True
    else:
        return False


# Letter in word but not at specified position
def notAtPos(pos, letters):
    if not letters:
        return True
    for letter in letters:
        if word[pos - 1] != letter:
            if not contains(letter):
                return False
        else:
            return False
    return True


# Contains letters
def contains(letters):
    for letter in letters:
        if letter not in word:
            return False
    return True


# Does not contain letters
def doesNotContain(letters):
    for letter in letters:
        if letter in word:
            return False
    return True


# Find possible words
possibleWords = []
for word in words:
    if hasLength(5) and doesNotContain('') and contains('') and \
            ___atPos(1, '') and ___atPos(2, '') and ___atPos(3, '') and ___atPos(4, '') and ___atPos(5, '') and \
            notAtPos(1, '') and notAtPos(2, '') and notAtPos(3, '') and notAtPos(4, '') and notAtPos(5, ''):
        possibleWords.append(word)


# Display next best word
print("All possible words (ordered by likelihood):")
for word in possibleWords:
    print(word)
print()
print(str(len(possibleWords)) + " possible words")
if len(possibleWords) == 39933:
    print("Next suggested word: roate")
else:
    print("Next suggested word: " + possibleWords[0])