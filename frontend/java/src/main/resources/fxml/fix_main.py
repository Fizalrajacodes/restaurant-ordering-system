with open(r"C:\Users\fizal\OneDrive\Desktop\MY PROJECTS\RESTURANT ORDERING SYSTEM\restaurant-ordering-system\frontend\java\src\main\resources\fxml\main.fxml", "r", encoding="utf-8") as f:
    content = f.read()
content = content.replace('fillWidth="true" ', '')
content = content.replace('fillWidth="true"', '')
with open(r"C:\Users\fizal\OneDrive\Desktop\MY PROJECTS\RESTURANT ORDERING SYSTEM\restaurant-ordering-system\frontend\java\src\main\resources\fxml\main.fxml", "w", encoding="utf-8") as f:
    f.write(content)
print("Removed fillWidth")