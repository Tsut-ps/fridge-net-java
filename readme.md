# 冷蔵庫の品を保存する通信型アプリ（CUI）

![image](https://user-images.githubusercontent.com/73014392/230713467-10e13b07-972c-4057-9742-024ec44faf8e.png)

## これ is 何

大学で Java を扱う授業があり、通信/ソケットを使用するタイプのアプリを創作して作成してみる という自由課題があったため、そこで作成したコードです。  
データベース作成は今回の趣旨と外れる（Javaの通信方法を理解するのが目的）ため、簡易的な実装（txtファイル）になっています。  
※例外処理が甘めです。研究目的以外で、そのままの状態で使用することを禁じます。

## 特徴

- 冷蔵庫の食材を記録する
    - 「食材」「個数」「賞味/消費期限」を入力可
- 残り日数の少ないものを視覚的に、一括で確認できる
- 複数の端末から閲覧や操作が可能（※）
    - 保存するサーバーと操作するクライアントが別

※同時接続は不可

## 必須環境

- 実行に Java 11 もしくは 16 が必要
- サーバーとクライアントの両方が必要
    - テストで使用する分には同端末内（localhost）でも接続可能
    - 接続先は最初に指定する必要がある

## 使用方法とスクリーンショット

使用方法や実際の画像は [Wiki](https://github.com/Tsut-ps/fridge-net-java/wiki) に記載しています。

## 詳しい仕様

### 処理の流れ

ファイルはサーバー側に保存して、その都度サーバー側がデータを読み込んだり書き換えたりする。
クライアント側はキーの入力など操作面を担う。

- サーバー側に保存するので、クライアント側が不正な方法でデータを変更できない。
- クライアント側は、サーバーへ情報の送受信のみを行う。

![image](https://user-images.githubusercontent.com/73014392/230713758-037057dc-99e3-4872-9c6f-54cead97ac02.png)

### 工夫した点

- 全体の例外処理以外にも処理を加えた。  
    日付やキーで不正な値が入力された場合に、再度実行するように処理。  
    → 再処理はクライアント側に処理をさせることで無駄な通信をしないように。

- わかりやすいように空白と区切り挿入、文字だけでも視覚的な表現に。  
    インターフェースが見やすいように空白行や区切り行を都度挿入。  
    ✕や■を出力することで、目で見てもわかりやすいよう工夫した。
