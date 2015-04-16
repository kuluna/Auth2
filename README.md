Auth2
====

![phone.png](https://bitbucket.org/repo/a9q964/images/3109687799-phone.png)
![wear.png](https://bitbucket.org/repo/a9q964/images/453386965-wear.png)

Sorry, this document is Japanese ONLY.

Auth2はGoogleやDropboxが採用している2段階認証に使用する数字6けたのキーを生成するアプリです。  

## 動作環境
* Android 4.1以上(Wearとの連携にはAndroid4.3以上)

## 機能

* 1分毎に認証キーを生成
* 複数アカウント対応(キーをリストで表示)
* キーの名前を自由に変更可能
* オフライン動作対応()
* __Android Wear対応__

### Android Wear
Auth2はAndroid Wearに対応しています。Android Wearと接続済みのAndroid4.3以上の端末にインストールするとAndroid WearにAuth2アプリが追加されます。  
スマートフォンで追加した認証キーをAndroid Wear単体で確認することができます。

## ビルド
Android SDKを別途用意してください。

### Android Studio
プロジェクトをそのままAndroid Studioで読み込めばビルドできます。

### Gradle Wrapper
Windows(コマンドプロンプト)

```
cd [プロジェクトフォルダ]  
gradlew.bat build
```

Mac/Linux(ターミナル)

```
cd [プロジェクトフォルダ]
./gradlew build
```

## License
Apache License 2.0