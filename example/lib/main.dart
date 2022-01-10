import 'dart:async';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:zendesk/zendesk.dart';

void main() => runApp(new MyApp());

const ZendeskAccountKey = '4ZPFfMXvpFAePMfuM2S04efeep86kkh2';
const ZendeskAppId = '376245032375951361';

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final Zendesk zendesk = Zendesk();

  @override
  void initState() {
    super.initState();
    initZendesk();
  }

  // Zendesk is asynchronous, so we initialize in an async method.
  Future<void> initZendesk() async {
    zendesk
        .init(ZendeskAccountKey, ZendeskAppId, deviceToken: "1234")
        .then((r) {
      print('init finished');
    }).catchError((e) {
      print('failed with error $e');
    });

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    // But we aren't calling setState, so the above point is rather moot now.
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: <Widget>[
              RaisedButton(
                child: Text('Set User Info'),
                onPressed: () async {
                  zendesk
                      .setVisitorInfo(
                    name: 'My Name',
                    phoneNumber: '323-555-1212',
                  )
                      .then((r) {
                    print('setVisitorInfo finished');
                  }).catchError((e) {
                    print('error $e');
                  });
                },
              ),
              if (Platform.isIOS)
                RaisedButton(
                  child: Text('Start Chat (styled)'),
                  onPressed: () async {
                    zendesk
                        .startChat(
                      iosNavigationBarColor: Colors.red,
                      iosNavigationTitleColor: Colors.yellow,
                    )
                        .then((r) {
                      print('startChat finished');
                    }).catchError((e) {
                      print('error $e');
                    });
                  },
                ),
              RaisedButton(
                child: Text('Add Tags [a,b,c]'),
                onPressed: () async {
                  zendesk.addVisitorTags(['a', 'b', 'c']).then((_) {
                    print('addTags Finished');
                  }).catchError((e) {
                    print('error $e');
                  });
                },
              ),
              RaisedButton(
                child: Text('Remove Tags [b,c]'),
                onPressed: () async {
                  zendesk.removeVisitorTags(['b', 'c']).then((_) {
                    print('removeTags Finished');
                  }).catchError((e) {
                    print('error $e');
                  });
                },
              ),
              RaisedButton(
                child: Text('Start Chat'),
                onPressed: () async {
                  zendesk.startChat().then((r) {
                    print('startChat finished');
                  }).catchError((e) {
                    print('error $e');
                  });
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}
