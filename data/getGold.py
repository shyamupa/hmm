with open("HW6.gold.txt") as f:
    for line in f:
        for word in line.split(" "):
            if '_' in word:
                print word.split('_')[1],
            else:
                print word,
                
        
