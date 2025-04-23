**Before running**  
To connect to MongoDBCompass use the following connection string: mongodb+srv://user2:Password@iocluster.mitrd.mongodb.net/  
Install MinIO at your machine.  
Before running the backend use the following command to run MinIO (in the location where MinIO is installed): ".\minio.exe server  
C:\Users\Wojtek\Desktop\iisi\20242025\io\backend_io\src\main\java\com\backend\project\storage --console-address :9001"  
Put the correct path to the 'files' folder in our project (it is located in src\main\java\com\backend\project\storage folder).  
  
Should it be your first time to run MinIO don't forget to open [localhost:9000](http://localhost:9001/browser), log in using default credentials (i.e. login: minioadmin, password: minioadmin), go to 'Access keys' (you will find it on the left sidebar)  
![image](https://github.com/user-attachments/assets/632af66d-ee04-4c0f-8ed5-913bfe204970)  
and create a new access key  
![image](https://github.com/user-attachments/assets/4e574006-6f07-4f12-8f9f-136391cbb9a3)  
then place newly created credentials into the application.properties file in our project  
![image](https://github.com/user-attachments/assets/7b61972c-cb94-4ded-932e-681b94d5f779)  
Ensure that every time you run the project YOUR credentials are placed there (once you set them up they remain the same, so you don't have to repeat the procedure every time you run backend. Just keep the credentials you created while running backend for the first time in the safe place and use them when necessary.)  

  
**After running**  
Once you add/delete/change any picture in the project don't forget to push the code to GitHub repository!!!! It is important to keep files folder consistent!!!  
