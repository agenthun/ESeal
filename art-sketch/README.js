http://www.freight-track.com/webservice/FreightTrackWebService.asmx/?&language=l
    {
        GetTokenByUserNameAndPassword 登陆
        	CreateEnterpriseManager 创建企业管理用户
        	FeedbackToFreightTrack 反馈
        GetFreightInfoByToken 根据Token获取所有在途中的货物信息
        	ModifyUserInfo 修改用户信息 - ModifyPassword
        ResetImplement 设置终端参数
        GetAllDynamicData 获取集装箱动态数据
        GetDynamicDataDetail 获取某一具体动态数据
        mFreightTrackPathURLFormat - #根据ContainerId获取轨迹 http://www.freight-track.com/BaiduMap/FreightTrackPath.aspx?Token=%@&Type=%@&ContainerId=%@
            mFreightTrackChartURLFormat - http://www.freight-track.com/Chart/FreightTrackChart.aspx?Token=%@&Type=%@&ContainerId=%@
        GetFreightInfoByImplementID 根据设备ID获取货物信息
        OpenContainer 开箱操作
        CloseContainer 关箱操作(海关 / 普通用户)
    }

#登陆，获取Token
http://www.freight-track.com/webservice/FreightTrackWebService.asmx/GetTokenByUserNameAndPassword?userName=demodemo&password=123456&language=l
    < string xmlns = "http://www.freight-track.com/" > {
        "EFFECTIVETOKEN": 1,
        "EMAIL": "56@163.com",
        "ENTERPRISE": "ECNU",
        "ERRORINFO": "",
        "FREIGHTOWNER": "JX",
        "IMGURL": "",
        "ISEMAIL": "0",
        "ISSMS": "1",
        "MOBILE": "18621584845",
        "REALNAME": "JX",
        "RESULT": 1,
        "ROLEID": "5",
        "TOKEN": "e9777dd2396947e1a97ee95a14c73432",
        "USERID": "5",
        "USERNAME": "demodemo"
    } < /string>


#反馈
http://www.freight-track.com/webservice/FreightTrackWebService.asmx/FeedbackToFreightTrack?advice=to much USELESS information&token=4a88c51e0e154cee8a6942f4a700e47e&language=l
< string xmlns = "http://www.freight-track.com/" > {
    "EFFECTIVETOKEN": 0,
    "ERRORINFO": "1;",
    "RESULT": 0
} < /string>


#根据Token获取所有在途中的货物信息
http://www.freight-track.com/webservice/FreightTrackWebService.asmx/GetFreightInfoByToken?token=5708cf01a08d448c85bb52c7bbe120fd&language=l
    < string xmlns = "http://www.freight-track.com/" > {
        "Result": [{
            "RESULT": 1,
            "EFFECTIVETOKEN": 1,
            "ERRORINFO": ""
        }],
        "Details": [{
            "ContainerId": "1073",
            "ContainerNo": "yangshan1130",
            "FreightName": "device",
            "Origin": "putuo",
            "Frequency": "1",
            "TempThreshold": "50",
            "HumThreshold": "80",
            "VibThreshold": "5.0",
            "Operationer": "demodemo",
            "Status": "1"
        }, {
            "ContainerId": "1072",
            "ContainerNo": "GHJF5774576",
            "FreightName": "iphone",
            "Origin": "sh",
            "Frequency": "1",
            "TempThreshold": "35",
            "HumThreshold": "80",
            "VibThreshold": "3.0",
            "Operationer": "demodemo",
            "Status": "1"
        }, {
            "ContainerId": "1071",
            "ContainerNo": "EDAA2815675",
            "FreightName": "cd",
            "Origin": "sh",
            "Frequency": "1",
            "TempThreshold": "35",
            "HumThreshold": "80",
            "VibThreshold": "3.0",
            "Operationer": "demodemo",
            "Status": "1"
        }, {
            "ContainerId": "1070",
            "ContainerNo": "FL0921",
            "FreightName": "test",
            "Origin": "Starwood hotel",
            "Frequency": "1",
            "TempThreshold": "40",
            "HumThreshold": "80",
            "VibThreshold": "2.5",
            "Operationer": "demodemo",
            "Status": "1"
        }, {
            "ContainerId": "1067",
            "ContainerNo": "123",
            "FreightName": "a",
            "Origin": "s",
            "Frequency": "1",
            "TempThreshold": "35",
            "HumThreshold": "60",
            "VibThreshold": "2.0",
            "Operationer": "demodemo",
            "Status": "0"
        }, {
            "ContainerId": "1064",
            "ContainerNo": "Swiss0414",
            "FreightName": "Computer",
            "Origin": "Zurich",
            "Frequency": "1",
            "TempThreshold": "35",
            "HumThreshold": "55",
            "VibThreshold": "3.5",
            "Operationer": "demodemo",
            "Status": "0"
        }, {
            "ContainerId": "1059",
            "ContainerNo": "SIPG",
            "FreightName": "Apples",
            "Origin": "SH",
            "Frequency": "1",
            "TempThreshold": "33",
            "HumThreshold": "66",
            "VibThreshold": "2.0",
            "Operationer": "demodemo",
            "Status": "2"
        }, {
            "ContainerId": "1058",
            "ContainerNo": "CCLU1234567",
            "FreightName": "Device",
            "Origin": "SH",
            "Frequency": "1",
            "TempThreshold": "33",
            "HumThreshold": "66",
            "VibThreshold": "2.0",
            "Operationer": "demodemo",
            "Status": "0"
        }, {
            "ContainerId": "1057",
            "ContainerNo": "CCLU",
            "FreightName": "Device",
            "Origin": "SH",
            "Frequency": "1",
            "TempThreshold": "33",
            "HumThreshold": "65",
            "VibThreshold": "2.0",
            "Operationer": "demodemo",
            "Status": "1"
        }, {
            "ContainerId": "1056",
            "ContainerNo": "FADS1234567",
            "FreightName": "Computer",
            "Origin": "SH",
            "Frequency": "1",
            "TempThreshold": "30",
            "HumThreshold": "70",
            "VibThreshold": "2.0",
            "Operationer": "demodemo",
            "Status": "1"
        }, {
            "ContainerId": "1055",
            "ContainerNo": "TTLU1234567",
            "FreightName": "Apple",
            "Origin": "SH",
            "Frequency": "1",
            "TempThreshold": "30",
            "HumThreshold": "70",
            "VibThreshold": "2.0",
            "Operationer": "demodemo",
            "Status": "1"
        }]
    } < /string>


#修改用户信息
http://www.freight-track.com/webservice/FreightTrackWebService.asmx/ModifyUserInfo?token=4a88c51e0e154cee8a6942f4a700e47e&password=123456&email=email@email.com&enterprise=ECNU&realName=JX&mobile=18621584845&isAcceptEmail=0&isAcceptSMS=0&language=l
< string xmlns = "http://www.freight-track.com/" > [{
    "RESULT": 1,
    "EFFECTIVETOKEN": 1,
    "ERRORINFO": ""
}] < /string>


#设置终端参数
"ContainerId": "1070",
http://www.freight-track.com/webservice/FreightTrackWebService.asmx/ResetImplement?token=4a88c51e0e154cee8a6942f4a700e47e&containerId=1070&frequency=2&tempThreshold=0&humThreshold=0&vibThreshold=0&language=l
< string xmlns = "http://www.freight-track.com/" > {
    "Result": [{
        "RESULT": 0,
        "EFFECTIVETOKEN": 1,
        "ERRORINFO": "14;"
    }]
} < /string>
// http://www.freight-track.com/webservice/FreightTrackWebService.asmx/ResetImplement?token=4a88c51e0e154cee8a6942f4a700e47e&imPlementID=12345678&cycletime=60&windowWidth=30&channelMode=1&language=l


#获取集装箱动态数据
http://www.freight-track.com/webservice/FreightTrackWebService.asmx/GetAllDynamicData?token=4a88c51e0e154cee8a6942f4a700e47e&containerId=1070&currentPageIndex=1&language=l
< string xmlns = "http://www.freight-track.com/" > {
    "Result": [{
        "RESULT": 1,
        "EFFECTIVETOKEN": 1,
        "ERRORINFO": "",
        "TOTAL": 124,
        "COUNTPAGES": 7
    }],
    "Details": [{
        "PositionId": "269014",
        "CreateDatetime": "2015/11/19 22:55:31",
        "PositionName": "",
        "Coordinate": "31.2301832666667,121.406276423333",
        "ActionType": "1",
        "SecurityLevel": "1",
        "Temperature": "19.0 ",
        "Humidity": "66.0 ",
        "Vibration": "0.0,0.1,0.9 "
    }, {
        "PositionId": "269013",
        "CreateDatetime": "2015/11/19 22:54:01",
        "PositionName": "",
        "Coordinate": "31.23007212,121.406226756667",
        "ActionType": "1",
        "SecurityLevel": "1",
        "Temperature": "19.0 ",
        "Humidity": "67.0 ",
        "Vibration": "0.0,0.1,0.8 "
    }, {
        "PositionId": "269012",
        "CreateDatetime": "2015/9/21 10:11:54",
        "PositionName": "1701 S Federal Hwy, Fort Lauderdale, FL 33316, USA",
        "Coordinate": "26.0996598333333,-80.1369595",
        "ActionType": "1",
        "SecurityLevel": "1",
        "Temperature": "23.0 ",
        "Humidity": "59.0 ",
        "Vibration": "0.1,0.1,1.0 "
    }, {
        "PositionId": "269011",
        "CreateDatetime": "2015/9/21 10:10:24",
        "PositionName": "505 Mariner Dr, Fort Lauderdale, FL 33316, USA",
        "Coordinate": "26.0995748333333,-80.1371556666667",
        "ActionType": "1",
        "SecurityLevel": "1",
        "Temperature": "23.0 ",
        "Humidity": "59.0 ",
        "Vibration": "0.1,0.0,0.9 "
    }, {
        "PositionId": "269010",
        "CreateDatetime": "2015/9/21 10:08:59",
        "PositionName": "505 Mariner Dr, Fort Lauderdale, FL 33316, USA",
        "Coordinate": "26.0994548333333,-80.1371926666667",
        "ActionType": "1",
        "SecurityLevel": "1",
        "Temperature": "23.0 ",
        "Humidity": "59.0 ",
        "Vibration": "0.0,0.1,0.9 "
    }, {
        "PositionId": "269009",
        "CreateDatetime": "2015/9/21 10:07:29",
        "PositionName": "500 SE 17th St, Fort Lauderdale, FL 33316, USA",
        "Coordinate": "26.0999105,-80.1377163333333",
        "ActionType": "1",
        "SecurityLevel": "1",
        "Temperature": "23.0 ",
        "Humidity": "59.0 ",
        "Vibration": "0.1,0.0,0.9 "
    }, {
        "PositionId": "269008",
        "CreateDatetime": "2015/9/21 10:06:03",
        "PositionName": "1701 S Federal Hwy, Fort Lauderdale, FL 33316, USA",
        "Coordinate": "26.0996288333333,-80.137055",
        "ActionType": "1",
        "SecurityLevel": "1",
        "Temperature": "23.0 ",
        "Humidity": "59.0 ",
        "Vibration": "0.1,0.0,0.9 "
    }, {
        "PositionId": "269007",
        "CreateDatetime": "2015/9/21 10:04:33",
        "PositionName": "505 Mariner Dr, Fort Lauderdale, FL 33316, USA",
        "Coordinate": "26.099379,-80.1373846666667",
        "ActionType": "1",
        "SecurityLevel": "1",
        "Temperature": "23.0 ",
        "Humidity": "59.0 ",
        "Vibration": "0.1,0.0,0.9 "
    }, {
        "PositionId": "269006",
        "CreateDatetime": "2015/9/21 10:03:06",
        "PositionName": "1701 S Federal Hwy, Fort Lauderdale, FL 33316, USA",
        "Coordinate": "26.0996288333333,-80.137055",
        "ActionType": "1",
        "SecurityLevel": "1",
        "Temperature": "23.0 ",
        "Humidity": "59.0 ",
        "Vibration": "0.1,0.0,0.9 "
    }, {
        "PositionId": "269005",
        "CreateDatetime": "2015/9/21 10:01:36",
        "PositionName": "505 Mariner Dr, Fort Lauderdale, FL 33316, USA",
        "Coordinate": "26.0995503333333,-80.1372128333333",
        "ActionType": "1",
        "SecurityLevel": "1",
        "Temperature": "23.0 ",
        "Humidity": "59.0 ",
        "Vibration": "0.1,0.0,0.9 "
    }, {
        "PositionId": "269004",
        "CreateDatetime": "2015/9/21 10:00:11",
        "PositionName": "505 Mariner Dr, Fort Lauderdale, FL 33316, USA",
        "Coordinate": "26.099597,-80.1372586666667",
        "ActionType": "1",
        "SecurityLevel": "1",
        "Temperature": "23.0 ",
        "Humidity": "60.0 ",
        "Vibration": "0.3,0.8,0.4 "
    }, {
        "PositionId": "269003",
        "CreateDatetime": "2015/9/21 9:58:41",
        "PositionName": "1701 S Federal Hwy, Fort Lauderdale, FL 33316, USA",
        "Coordinate": "26.0997041666667,-80.1370891666667",
        "ActionType": "1",
        "SecurityLevel": "1",
        "Temperature": "23.0 ",
        "Humidity": "59.0 ",
        "Vibration": "0.0,0.9,0.3 "
    }, {
        "PositionId": "269001",
        "CreateDatetime": "2015/9/21 9:57:15",
        "PositionName": "1701 S Federal Hwy, Fort Lauderdale, FL 33316, USA",
        "Coordinate": "26.099662,-80.1369553333333",
        "ActionType": "1",
        "SecurityLevel": "1",
        "Temperature": "23.0 ",
        "Humidity": "59.0 ",
        "Vibration": "0.0,0.9,0.3 "
    }, {
        "PositionId": "269002",
        "CreateDatetime": "2015/9/21 9:55:45",
        "PositionName": "1701 S Federal Hwy, Fort Lauderdale, FL 33316, USA",
        "Coordinate": "26.0998368333333,-80.1370041666667",
        "ActionType": "1",
        "SecurityLevel": "1",
        "Temperature": "23.0 ",
        "Humidity": "59.0 ",
        "Vibration": "0.0,0.9,0.3 "
    }, {
        "PositionId": "269000",
        "CreateDatetime": "2015/9/21 9:54:20",
        "PositionName": "505 Mariner Dr, Fort Lauderdale, FL 33316, USA",
        "Coordinate": "26.099438,-80.1372905",
        "ActionType": "1",
        "SecurityLevel": "1",
        "Temperature": "23.0 ",
        "Humidity": "58.0 ",
        "Vibration": "0.0,0.9,0.3 "
    }, {
        "PositionId": "268999",
        "CreateDatetime": "2015/9/21 9:52:50",
        "PositionName": "1701 S Federal Hwy, Fort Lauderdale, FL 33316, USA",
        "Coordinate": "26.0999148333333,-80.1370173333333",
        "ActionType": "1",
        "SecurityLevel": "1",
        "Temperature": "23.0 ",
        "Humidity": "58.0 ",
        "Vibration": "0.0,0.9,0.3 "
    }, {
        "PositionId": "268997",
        "CreateDatetime": "2015/9/21 9:51:24",
        "PositionName": "505 Mariner Dr, Fort Lauderdale, FL 33316, USA",
        "Coordinate": "26.0996343333333,-80.1372433333333",
        "ActionType": "1",
        "SecurityLevel": "1",
        "Temperature": "23.0 ",
        "Humidity": "58.0 ",
        "Vibration": "0.0,0.9,0.3 "
    }, {
        "PositionId": "268998",
        "CreateDatetime": "2015/9/21 9:49:54",
        "PositionName": "1701 S Federal Hwy, Fort Lauderdale, FL 33316, USA",
        "Coordinate": "26.0998136666667,-80.1369315",
        "ActionType": "1",
        "SecurityLevel": "1",
        "Temperature": "23.0 ",
        "Humidity": "58.0 ",
        "Vibration": "0.0,0.9,0.3 "
    }, {
        "PositionId": "268995",
        "CreateDatetime": "2015/9/21 9:48:28",
        "PositionName": "505 Mariner Dr, Fort Lauderdale, FL 33316, USA",
        "Coordinate": "26.0995895,-80.1371998333333",
        "ActionType": "1",
        "SecurityLevel": "1",
        "Temperature": "23.0 ",
        "Humidity": "58.0 ",
        "Vibration": "0.0,0.9,0.3 "
    }, {
        "PositionId": "268996",
        "CreateDatetime": "2015/9/21 9:46:58",
        "PositionName": "1701 S Federal Hwy, Fort Lauderdale, FL 33316, USA",
        "Coordinate": "26.0996871666667,-80.1372126666667",
        "ActionType": "1",
        "SecurityLevel": "1",
        "Temperature": "23.0 ",
        "Humidity": "58.0 ",
        "Vibration": "0.0,0.9,0.3 "
    }]
} < /string>

#获取某一具体动态数据
http://www.freight-track.com/webservice/FreightTrackWebService.asmx/GetDynamicDataDetail?token=4a88c51e0e154cee8a6942f4a700e47e&positionId=268996&language=l
< string xmlns = "http://www.freight-track.com/" > {
    "Result": [{
        "RESULT": 1,
        "EFFECTIVETOKEN": 1,
        "ERRORINFO": ""
    }],
    "Details": [{
        "PositionId": "268996",
        "CreateDatetime": "2015/9/21 9:46:58",
        "PositionName": "1701 S Federal Hwy, Fort Lauderdale, FL 33316, USA",
        "Coordinate": "26.0996871666667,-80.1372126666667",
        "ActionType": "1",
        "SecurityLevel": "1",
        "Temperature": "23.0 ",
        "Humidity": "58.0 ",
        "Vibration": "0.0,0.9,0.3 "
    }]
} < /string>


#根据ContainerId获取轨迹
"ContainerId": "1070",
http://www.freight-track.com/BaiduMap/FreightTrackPath.aspx?token=4a88c51e0e154cee8a6942f4a700e47e&Type=0&ContainerId=1070&language=l


// #mFreightTrackChartURLFormat
// "ContainerId": "1070",
// http://www.freight-track.com/Chart/FreightTrackChart.aspx?Token=4a88c51e0e154cee8a6942f4a700e47e&Type=0&ContainerId=1070&language=l


#根据设备ID获取货物信息
http://www.freight-track.com/webservice/FreightTrackWebService.asmx/GetFreightInfoByImplementID?token=4a88c51e0e154cee8a6942f4a700e47e&implementID=12345678&language=l
< string xmlns = "http://www.freight-track.com/" > {
    "Result": [{
        "RESULT": 0,
        "EFFECTIVETOKEN": 1,
        "ERRORINFO": "12;"
    }],
    "Details": [{}]
} < /string>


#开箱操作 - 获取MAC
http://www.freight-track.com/webservice/FreightTrackWebService.asmx/OpenContainer?token=4a88c51e0e154cee8a6942f4a700e47e&implementID=12345678&random=1234&language=l
< string xmlns = "http://www.freight-track.com/" > {
    "EFFECTIVETOKEN": 1,
    "ERRORINFO": "",
    "MAC": "8D 61 1C 8D 30 78 E8 C0 C7 19 65 D2 AE FE 18 D3 5A 6C 6A FB",
    "RESULT": 1
} < /string>

#关箱操作(海关 / 普通用户) - 获取MAC
http://www.freight-track.com/webservice/FreightTrackWebService.asmx/CloseContainer?token=4a88c51e0e154cee8a6942f4a700e47e&implementID=12345678&random=1234&language=l
< string xmlns = "http://www.freight-track.com/" > {
    "EFFECTIVETOKEN": 1,
    "ERRORINFO": "",
    "MAC": "8D 61 1C 8D 30 78 E8 C0 C7 19 65 D2 AE FE 18 D3 5A 6C 6A FB",
    "RESULT": 1
} < /string>


