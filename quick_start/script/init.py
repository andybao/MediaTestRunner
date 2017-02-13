#!/usr/bin/env python
# coding=utf-8

import os
import time
import datetime

ADB = 'adb'

script_dir_PC = './'
tool_dir_PC = '../tool/'
log_dir_PC = '../log/'
content_dir_PC = '../content/'
playlist_dir_PC = '../playlist/'

tool_dir_device = '/sdcard/testrunner/'
content_dir_device = tool_dir_device + 'content'
log_dir_device = tool_dir_device + 'log'
playlist_dir_device = tool_dir_device + 'playlist'

setting_file_name = 'setting.txt'
log_file_name = 'run_case.log'
playlist_file_name = 'video_default_playlist.txt'
tool_name = 'app-debug.apk'
content_name = 'SampleVideo_720x480_10mb.mp4'

def getDevice():

    global DEVICE

    writeLog('->0) Get device from setting file')

    setting_file_dir = script_dir_PC + setting_file_name

    for ln in open (setting_file_dir).readlines():
        if ln.find('DEVICE') >= 0:
            DEVICE = ln.split('=')[1].strip()

def writeLog(_comments):

    m_log = log_dir_PC + log_file_name
    m_logFile = open(m_log, 'a')
    m_time = datetime.datetime.now().strftime('%Y/%m/%d %H:%M:%S')
    m_comments = m_time + '\t' + _comments
    m_logFile.write(m_comments + '\n')
    m_logFile.close()
    print m_comments

def connectToDevice():

    writeLog('->1) Try to connect to test device: %s' % (DEVICE))

    os.popen(ADB + ' kill-server')
    time.sleep(10)

    devices = os.popen(ADB + ' devices').readlines()
    deviceList = ''
    for device in devices:
        deviceList += device

    if not deviceList.find(DEVICE) >= 0:
        writeLog('Do not connect to device: ' + DEVICE)
        return False

    return True

def createDirInDevice():

    if not connectToDevice():
        return False
    time.sleep(3)

    writeLog('->2) Create test dir in device')
    cmd_create_dir = '%s -s %s shell mkdir ' % (ADB, DEVICE)

    os.popen(cmd_create_dir + tool_dir_device)
    time.sleep(1)
    os.popen(cmd_create_dir + content_dir_device)
    time.sleep(1)
    os.popen(cmd_create_dir + log_dir_device)
    time.sleep(1)
    os.popen(cmd_create_dir + playlist_dir_device)
    time.sleep(1)

    return True

def copyContentToDevice():

    writeLog('->3) Copy test content %s to device' % content_name)

    content = content_dir_PC + content_name
    cmd_copy_content = '%s -s %s push %s %s' % (ADB, DEVICE, content, content_dir_device)

    os.popen(cmd_copy_content)
    time.sleep(10)

def copyPlaylistToDevice():

    writeLog('->4) Copy playlist %s to device' % playlist_file_name)

    playlist = playlist_dir_PC + playlist_file_name
    cmd_copy_playlist = '%s -s %s push %s %s' % (ADB, DEVICE, playlist, playlist_dir_device)

    os.popen(cmd_copy_playlist)
    time.sleep(3)

def installTestRunner():

    writeLog('->5) Install testrunner apk')

    test_runner = tool_name

    for f in os.listdir(tool_dir_PC):
        if f.find(test_runner) >= 0:
            test_runner = tool_dir_PC + f

    cmd_install_tool = '%s -s %s install %s' % (ADB, DEVICE, test_runner)
    os.popen(cmd_install_tool)

def startMainActivity():

    writeLog('->6) Wait 10s for user accept permission')
    cmd_start_activity = '%s -s %s shell am start -n com.andybao.mediatestrunner/.framework.ui.MainActivity' % (ADB, DEVICE)
    os.popen(cmd_start_activity)
    time.sleep(10)

    writeLog('->7) Env init is finished')

if __name__ == '__main__':

    getDevice()
    createDirInDevice()
    copyContentToDevice()
    copyPlaylistToDevice()
    installTestRunner()
    startMainActivity()
