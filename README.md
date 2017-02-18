# Author
Andy Bao [LinkedIn](https://ca.linkedin.com/in/baowenyu)

A dedicated professional with 8+ years experience in Mobile OS based software applications automation and manual testing. Specializes in Android automation testing tool development and maintenance with Java/Python. Now seeking to contribute my experience, skills and expertise as software testing tool developer in Toronto.

# YouTube Video
You will know how this tool looks like from [MediaTestRunner Video Demo](https://youtu.be/GQjkDOOLegE)

# Introduction
- MediaTestRunner server use Android MediaPlayerControl to test media codec, it assert result by MediaPlayer method.
- MediaTestRunner client use python script to initialize environment, parse log and generate report.
- You can get more info from [demo_introduction](./quick_start/demo_introduction.pdf)

# Android detail
|Layout|ListView|
|DB    |SQLite  |

# Quick Start
1. Download ./quick_start folder
2. Connect device to PC or open simulator, get serial number by 'adb devices'
3. Update your serial number to ./quick_start/script/setting.txt
4. Move to ./quick_start/script folder in terminal
5. Run init.py by python, accept permission request in device
6. Run run_test_github.py by python
