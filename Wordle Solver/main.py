import csv

# Jason Niemann's Wordle Solver
# Wordle website: https://www.nytimes.com/games/wordle/index.html

# A simple program to solve Wordles. 'words.csv' contains a list of 330k+ English words sorted from most to least
# common. The file is read in and the words are stored in an array. The if statement on line 64 can be used to
# deduce which words fit the Wordle criteria.

# CSV to array
file = open("words.csv", "r")
reader = csv.reader(file)
words = []
for row in reader:
    words.append(row[0])


# Is x Letters
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
    # TODO: Use the doesNotContain, ___atPos, and notAtPos functions to solve your Wordle
    # Place multiple letters between the quotation marks as necessary: ___atPos('hello')
    # You do not need to use the contains('') function as it is automatically called in other functions.

    # Be careful with double letters.
    # For example, if your last guess was 'plaza' and the 'a' at position 3 is correct, but the 'a' at position 5 is
    # incorrect, then you should mark both ___atPos(3, 'a') and notAtPos(5, 'a').
    # However, do NOT mark doesNotContain('a') as this would contradict the fact that ___atPos(3, 'a') is true.

    # Double check that your letters are in the appropriate spots!
    # Enjoy!
    if hasLength(5) and doesNotContain('') and contains('') and \
            ___atPos(1, '') and ___atPos(2, '') and ___atPos(3, '') and ___atPos(4, '') and ___atPos(5, '') and \
            notAtPos(1, '') and notAtPos(2, '') and notAtPos(3, '') and notAtPos(4, '') and notAtPos(5, ''):
        possibleWords.append(word)

# Display next most likely word and all possible words
print("All possible words (ordered by likelihood):")
for word in possibleWords:
    print(word)
print()
print(str(len(possibleWords)) + " possible words")
if len(possibleWords) == 39933:
    # According to various online sources, 'roate' is statistically the best first word.
    print("Next suggested word: roate")
else:
    print("Next suggested word: " + possibleWords[0])

# Future Project: Incorporate program into Google Chrome Extension. Start with 'manifest.json' file.
