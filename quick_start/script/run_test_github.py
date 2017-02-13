#!/usr/bin/env python
# coding=utf-8

import os
import time
import datetime

ADB = 'adb'

script_dir_PC = './'
log_dir_PC = '../log/'

tool_dir_device = '/sdcard/testrunner/'
content_dir_device = tool_dir_device + 'content'
log_dir_device = tool_dir_device + 'log'

setting_file_name = 'setting.txt'
log_file_name_PC = 'run_case.log'
log_file_name_device = "test_case_log.txt"
report_file = 'simulator_report.html'

start_run_time = ''
end_run_time = ''
totalCount = 0
failCount = 0
passCount = 0
failedCaseList = ''

header = '''<head>
<style type='text/css'e>
table {
    word-wrap:break-word;
    font-family:'Times New Roman', Times, serif;
}
td { max-width: 700px;
}
</style>
</head>'''

body = '''<table border='1'>
    <tr><th bgcolor='#cccccc'>Test Case</th>
        <th bgcolor='#cccccc'>Content</th>
        <th bgcolor='#cccccc'>Status</th>
        <th bgcolor='#cccccc'>Reason</th>
    </tr>'''

def getDevice():

    global DEVICE

    writeLog('->1) Get device from setting file')

    setting_file_dir = script_dir_PC + setting_file_name

    for ln in open (setting_file_dir).readlines():
        if ln.find('DEVICE') >= 0:
            DEVICE = ln.split('=')[1].strip()

def writeLog(_comments):

    m_log = log_dir_PC + log_file_name_PC
    m_logFile = open(m_log, 'a')
    m_time = datetime.datetime.now().strftime('%Y/%m/%d %H:%M:%S')
    m_comments = m_time + '\t' + _comments
    m_logFile.write(m_comments + '\n')
    m_logFile.close()
    print m_comments

def connectToDevice():

    writeLog('->1) Try to connet to test device %s' % (DEVICE))

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

def runTest():

    global start_run_time, end_run_time
    start_run_time = datetime.datetime.now().strftime('%Y/%m/%d %H:%M:%S')
    cases = 'TestVideoBasicFunction#TestVideoFailedOnPause'
    isFinished = False

    writeLog('->2) Delete log file in phone.')

    cmd_delete_test_runner_log = '%s -s %s shell rm %s/%s' % (ADB, DEVICE, log_dir_device, log_file_name_device)
    os.popen(cmd_delete_test_runner_log)
    time.sleep(5)

    writeLog('->3) Start service.')
    writeLog('->4) Run cases: ' + cases)

    cmd_start_service = '%s -s %s shell am startservice -n com.andybao.mediatestrunner/.framework.service.TestRunnerService -e TestCaseList ' % (ADB, DEVICE)
    os.popen(cmd_start_service + cases)
    time.sleep(5)

    cmd_pull_test_runner_log = '%s -s %s pull %s %s' % (ADB, DEVICE, log_dir_device + "/" + log_file_name_device, log_dir_PC + log_file_name_device)
    while not isFinished:
        os.popen(cmd_pull_test_runner_log)
        for f in os.listdir(log_dir_PC):
            if f.find('test_case_log') >= 0:
                f = log_dir_PC + f
                tmp = open(f).readlines()[-1]
                if tmp.find('Test Complete!') >= 0:
                    isFinished = True
                    writeLog('->5) Finish run the test case!')
                    break
                else:
                    writeLog('---Test is running. Wait for 60 seconds.---')
                    os.remove(f)
                    time.sleep(60)
                    continue

    end_run_time = datetime.datetime.now().strftime('%Y/%m/%d %H:%M:%S')

def parseTheWholeLog():
    writeLog('->6) Parse the whole log.')
    log_file = log_dir_PC + log_file_name_device
    isFinish = False
    while not isFinish:
        test_case_name = ''
        content_names = []
        results = []
        comments = []
        logFile = ''
        for ln in open(log_file).readlines():
            if ln.find('[Run Test Case]: ') >= 0:
                if ln.find('TotalLoop:') >= 0:
                    test_case_name = ln.split(']: ')[1].strip()
                    test_case_name = test_case_name.split(',')[0].strip()
                else:
                    test_case_name = ln.split(']: ')[1].strip()
            elif ln.find('Index:') >= 0:
                full_content_name = ln.split('Test File:')[1].strip()
                content_name = full_content_name.split("/")[-1]
                content_names.append(content_name)
            elif ln.find('---> TestCase Failed') >= 0:
                results.append('Failed')
                comments.append(ln.split('TestCase Failed')[1].strip().replace(': ', ''))
                if len(content_names) == 0:
                    content_names = [test_case_name]
                generate_body(test_case_name, content_names, results, comments)
                test_case_name = ''
                content_names = []
                results = []
                comments = []
            elif ln.find('---> TestCase Passed') >= 0:
                results.append('Passed')
                if len(content_names) == 0:
                    content_names = [test_case_name]
                if len(results) == 0:
                    results = ['']
                if len(comments) == 0:
                    comments = ['']
                generate_body(test_case_name, content_names, results, comments)
                test_case_name = ''
                content_names = []
                results = []
                comments = []
            elif ln.find('Test Complete!') > 0:
                isFinish = True

def generate_body(featureName, caseNames, results, comments):
    row = len(caseNames)
    global body
    global passCount
    global failCount
    global totalCount
    global failedCaseList
    featureName = featureName[4:]
    if row == 1:
        line = ''
        if results[0] == 'Passed':
            line = '''
    <tr>
        <th style='text-align: left' rowspan='1'>%s</th>
        <td >%s</td>
        <td bgcolor='green'>PASS</td>
        <td >&nbsp;</td>
    </tr>''' % (featureName, caseNames[0])
            passCount += 1
            totalCount += 1
        else:
            line = '''
    <tr>
        <th style='text-align: left' rowspan='1'>%s</th>
        <td >%s</td>
        <td bgcolor='red'>FAIL</td>
        <td >%s</td>
    </tr>''' % (featureName, caseNames[0], comments[0])
            failCount += 1
            totalCount += 1
            if failedCaseList.find(featureName) < 0:
                failedCaseList += 'Test' + featureName + '#'
        body += line
    else:
        line_1 = ''
        line_2 = ''
        if results[0] == 'Passed':
            line_1 = '''
    <tr>
        <th style='text-align: left' rowspan='%s'>%s</th>
        <td >%s</td>
        <td bgcolor='green'>PASS</td>
        <td >&nbsp;</td>
    </tr>''' % (row, featureName, caseNames[0])
            passCount += 1
            totalCount += 1
        else:
            line_1 = '''
    <tr>
        <th style='text-align: left' rowspan='%s'>%s</th>
        <td >%s</td>
        <td bgcolor='red'>FAIL</td>
        <td >%s</td>
    </tr>''' % (row, featureName, caseNames[0], comments[1])
            failCount += 1
            totalCount += 1
            if failedCaseList.find(featureName) < 0:
                failedCaseList += 'Test' + featureName + '#'
        for i in range(1, row):
            l = ''
            if results[i] == 'Passed':
                l = '''
    <tr>
        <td >%s</td>
        <td bgcolor='green'>PASS</td>
        <td >&nbsp;</td>
    </tr>''' % (caseNames[i])
                passCount += 1
                totalCount += 1
            else:
                l = '''
    <tr>
        <td >%s</td>
        <td bgcolor='red'>FAIL</td>
        <td >%s</td>
    </tr>''' % (caseNames[i], comments[i])
                failCount += 1
                totalCount += 1
                if failedCaseList.find(featureName) < 0:
                    failedCaseList += 'Test' + featureName + '#'
            line_2 = line_2 + l
        body += line_1 + line_2

def generate_html():
    writeLog('->7) Generate the html report.')
    global body
    rep = '''<html>
    %s
<body>
    %s
    %s
    </table>
</body>
</html>''' % (header, generate_title(), body)
    try:
        if os.path.isfile(report_file):
            os.remove(report_file)
        f = open(report_file, 'w')
        f.write(rep)
        f.close()
    except Exception as e:
        print e
        return False
    body = '''<table border='1'>
    <tr><th bgcolor='#cccccc'>Test Case</th>
        <th bgcolor='#cccccc'>Content</th>
        <th bgcolor='#cccccc'>Status</th>
        <th bgcolor='#cccccc'>Reason</th>
    </tr>'''
    return True

def generate_title():
    m_title = ''
    if failCount == 0:
        totalResult = 'Pass'
    else:
        totalResult = 'Fail'

    m_title = 'Demo'

    title = '''<p><h1><a href=''>%s Test Result ( %s [ pass rate: %.2f%%, expected: 90%% ])</a></h1></p>
<table border='1'>
<tr>
    <th bgcolor='#cccccc'>Branch</th>
    <th bgcolor='#cccccc'>Start Time</th>
    <th bgcolor='#cccccc'>End Time</th>
    <th bgcolor='#cccccc'>Total</th>
    <th bgcolor='#cccccc'>Passed</th>
    <th bgcolor='#cccccc'>Failed</th>
</tr>
<tr>
    <td>TBD</td>
    <td>%s</td>
    <td>%s</td>
    <td>%s</td>
    <td>%s</td>
    <td>%s</td>
    </tr>
</table>
<hr/>''' % (m_title, totalResult, passCount * 100 / totalCount, start_run_time, end_run_time, totalCount, passCount, failCount)
    return title

def openReport():
    cmd_open_report = 'open ./' + report_file
    os.system(cmd_open_report)
    writeLog('->8) Open report')

if __name__ == '__main__':
    getDevice()
    runTest()
    parseTheWholeLog()
    generate_html()
    openReport()
