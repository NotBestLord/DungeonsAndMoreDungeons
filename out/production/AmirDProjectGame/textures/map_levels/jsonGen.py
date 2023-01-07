fileName = input("Enter File Name: ")
content = "{\n"
content = content + "  \"tileSize\": " + input("Enter Size Of Tile: ") + ",\n"
content = content + "  \"collision\": [\n"
c = 0
while input("Add Collision Box?(Y/N)") != "N" :
    if c > 0:
        content = content + ",\n"
    content = content + "    {\n"
    content = content + "      \"min\": {\n"
    content = content + "        \"x\": " + input("Enter Min X Tile: ") + ",\n"
    content = content + "        \"y\": " + input("Enter Min Y Tile: ") + "\n"
    content = content + "      },\n"
    content = content + "      \"max\": {\n"
    content = content + "        \"x\": " + input("Enter Max X Tile: ") + ",\n"
    content = content + "        \"y\": " + input("Enter Max Y Tile: ") + "\n"
    content = content + "      }\n"
    content = content + "    }"
content = content + "\n  ]"
content = content + "\n}"
open(fileName+".json", 'w').write(content)