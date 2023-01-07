start = int(input("start"))
end = int(input("end"))
i = start
out = ""
while i < end:
    out = out + str(i) + ","
    i+=1

print(out + str(i))
input()