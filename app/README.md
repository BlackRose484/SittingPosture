# 🪑 Sitting Posture

## 📌 Overview
**Sitting Posture** is a mobile application that utilizes AI to monitor users' sitting posture in real-time. The app detects incorrect postures and provides audio notifications with corrective feedback to help users maintain a healthy posture.

Our application informs users about their sitting posture status, identifying three incorrect postures: **Neck Bending, Leaning Forward, and Leaning Backward**, along with one correct sitting posture.

## ✨ Features
- **Real-time Posture Detection**: Uses AI to analyze sitting posture continuously.
- **Audio Notifications**: Alerts users when an incorrect posture is detected.
- **Custom Dataset**: Trained on a self-recorded and labeled dataset to improve accuracy.
- **High Accuracy**: Achieves 92.3% accuracy using the MobileNet model.

## 🛠 Technologies Used
- **Machine Learning**: MobileNet, TensorFlow
- **Computer Vision**: OpenCV
- **Mobile Development**: Java, XML, Gradle
- **Dataset Management**: DataLab, Python, Pandas

## ⚙️ Installation
### Prerequisites
- Python 3.8+
- TensorFlow
- OpenCV
- Java Development Kit (JDK)

### Part 1: Model - Python (Suggest PyCharm)
### Part 2: Application - Android Studio
1. Open Terminal in Android Studio
```
    git clone https://github.com/BlackRose484/SittingPosture.git
    cd SittingPosture 
```
2. Sync Gradle files
3. Run the application:
- Connect a physical Android device (ensure USB debugging is enabled) or start an Android emulator.
- Click on Run ( ▶️ ) in Android Studio and select your target device.
4. The app will launch on your device/emulator, and you can start using it!

## 🎮 Usage
1. Open the application.
2. Allow camera permissions.
3. Sit in front of the camera.
4. The app will detect your posture and notify you if any correction is needed.

## 📊 Dataset
The dataset was self-recorded and labeled to improve model performance. It includes various sitting postures categorized as correct and incorrect.
[link]
## 👥 Contributors
- Nguyen Ngoc Hung - Developer
- Lai Hoang Hiep - Leader
- Chu Huy Quang - Developer
- Le Minh Duc - Developer
## 📜 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📧 Contact
For inquiries, please contact [hungnbc2@gmail.com](mailto:hungnbc2@gmail.com).

