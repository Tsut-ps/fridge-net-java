import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class fridgeClient {
    public static final int PORT = 51234; // デフォルト値

    public static void main(String args[]) {

		// サーバーとクライアント間で通信を行うためのクラス
        Socket socket = null;

        try {
			
			System.out.println("――――――――――――――――"); // デバッグ情報

			long startTime = System.currentTimeMillis(); // サーバ接続時間計測

			if (args.length == 0) { // 引数が何もない
				System.out.println("# サーバー名とポート番号が指定されていないため、デフォルト値[localhostに51234番]で接続します。");
				socket = new Socket("localhost", PORT);		// args[0] に PORT で接続する
			} else if (args.length == 1) { // 引数が1つだけ(サーバー名のみ)
				System.out.println("# ポート番号が指定されていないため、デフォルト値[51234]を使用します。");
				socket = new Socket(args[0],51234);
			} else { // 引数が正常に指定された場合
				socket = new Socket(args[0],Integer.parseInt(args[1]));
			}

			// サーバ接続時間結果
			long connectionTime = System.currentTimeMillis() - startTime;
			String connectionStatus;
			if (connectionTime <= 30) connectionStatus = "良好";
			else if (connectionTime <= 200) connectionStatus = "通常";
			else connectionStatus = "不安定";

			// クライアントがサーバに接続したことを通知
            System.out.println("# 冷蔵庫データサーバAに接続しました。\n# \t- 接続先: " + socket.getRemoteSocketAddress() + "\n# \t- 接続状態: " + connectionStatus + "(" + connectionTime + "ms)\n――――――――――――――――");

			// サーバからの送信データを読み込む準備
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// サーバからの送信データを書き出す準備
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

			// キーボードから入力されるデータを読み込む準備
			BufferedReader kBIn = new BufferedReader(new InputStreamReader(System.in));
			
			// 冷蔵庫の中身確認
			// 初回デフォルト値(まずは1で実行する)
			// コマンド(キー)の入力はwhile文の最後で実行
			int isKey = 1;

			// 取り扱う変数を定義
			String name; int items;
			String date = "1900/01/01";	// 初期化用日付(コンパイルエラー対応)

			// 終了キー(4)を押すまで繰り返し実行
			while (isKey != 4) {

				// 入力された値をサーバに送信
				out.println(isKey);

				// 受け取ったコマンドによって処理を実行
				switch (isKey) {

					// サーバを通してファイルのデータを表示(1)
					case 1:
						// サーバから受け取った文字行を格納する変数
						String receivedLine;

						System.out.println("\n―――――――― 現在の冷蔵庫の中身です。");
						
						// サーバからデータを受信する
						while ( true ) {
							receivedLine = in.readLine();	// サーバから1行受信
							if (receivedLine.equals("EOF")) break;	// 終了コマンド(特定文字列)が受信されたときに終了
							System.out.println(receivedLine);	// ターミナルに情報表示
						}
						
						System.out.println("――――――――\n");
						break;

					// サーバを通してファイルのデータに1つ追加(2)
					case 2:
						// 日付パターン定義(uuuu → 元号や暦が関係しないyyyy)						
						DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("uuuu/MM/dd");

						System.out.print("\t商品名を入力 > ");
						name = kBIn.readLine();	// 商品名を入力
						out.println(name);	// サーバに送信

						System.out.print("\t個数を入力 > ");
						items = Integer.parseInt(kBIn.readLine());	// 個数を入力
						out.println(items);	// サーバに送信
						
						System.out.print("\t賞味/消費期限を入力(yyyy/MM/dd) > ");
						while (true) {	// 日付形式が正しくない場合に再実行
							try {
								date = kBIn.readLine();	// 期限を入力
								LocalDate.parse(date, dateFormat.withResolverStyle(ResolverStyle.STRICT));	// 正しい日付けかチェック
								break;	// 正しければwhile文を終了
							} catch (Exception dateError) {
								System.out.print("\t正しい日付形式(yyyy/MM/dd)で入力してください > ");	// 正しくない場合に警告表示
							}
						}
						out.println(date);	// サーバに送信
						System.out.println("追加しました。\n");
						break;

					// サーバを通してファイルのデータを1つ削除(3)
					case 3:
						System.out.println("――――――――");

						// サーバからデータを受信する
						while ( true ) {
							receivedLine = in.readLine();	// サーバから1行受信
							if (receivedLine.equals("EOF")) break;	// 終了コマンド(特定文字列)が受信されたときに終了
							System.out.println(receivedLine);	// ターミナルに情報表示
						}

						System.out.println("――――――――");
						System.out.print("どの項目を削除しますか(数字で選択) > ");

						// 削除する番号を入力
						int select = Integer.parseInt(kBIn.readLine());
						out.println(select);	// サーバに送信

						System.out.println("削除しました。\n");
						break;
				}

				// コマンド入力用に指示できる内容を表示
				System.out.print("[1: 期限の確認] [2: 書き込み] [3: 削除] [4: 終了]\n> ");

				// コマンド入力
				while ( true ) {	// 不正な値の場合に再実行
					try {
						// isKeyに受け取ったコマンド(キー)を格納
						isKey = Integer.parseInt(kBIn.readLine());
						break;	// 正しければwhile文を終了
					} catch (Exception keyError) {	// 数値以外が入力された場合
						System.out.print("正しい形式で入力してください(数字) > ");	// 警告表示
					}
				}
				if (isKey < 1 || isKey > 3 ) isKey = 4; // 範囲外対応
			}

			// 終了キー(4)を送信
			out.println(isKey);

			// ターミナルに終了を通知
			System.out.println("サーバへの通信を切断し、終了します。");

			// 閉じる
            out.close();
            socket.close();
		
		} catch (IOException e) { // 例外処理
            e.printStackTrace();
        }
    }
}
