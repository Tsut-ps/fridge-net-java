import java.io.*;
import java.net.*;
import java.nio.file.*;	// ファイル移動用(コピーして削除)
import java.time.Duration;	// 日付差表示用
import java.time.LocalDate;	// データの文字列を日付化する用
import java.time.LocalDateTime;	// 日付管理用
import java.time.format.DateTimeFormatter;	// データの文字列を日付化する用(フォーマット指定)

public class fridgeServer {
    public static final int PORT = 51234;
    
	public static void main(String args[]) {

		// サーバがクライアントからの接続を受け付けるためのクラス
        ServerSocket serverSocket = null;
		
		//サーバとクライアント間で通信を行うためのクラス
		Socket socket = null;
        
		try {
            // PORTで受け付けるサーバを作成
			serverSocket = new ServerSocket(PORT);
			
            // サーバが正常に起動したことを通知
			System.out.println("冷蔵庫データサーバAを起動しました / Port: " + serverSocket.getLocalPort());
			
			// クライアントからの要求を待ち続ける
            socket = serverSocket.accept();

			// クライアントがサーバが接続したことを通知
            System.out.println("クライアントが接続しました / IP: " + socket.getRemoteSocketAddress());

			//クライアントからの送受信されるデータを読み書きする準備
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			
			// コマンド受諾用キー
			int isKey;

			// データ保管用変数
			String name; int items; String date;

			// 終了キー(4)を押すまで繰り返し実行
			while ((isKey = Integer.parseInt(in.readLine())) != 4) {

				// ファイルがない場合に新規生成
				File file = new File("fridgeData.txt");
				if (!file.exists()) file.createNewFile();

				// ファイルから受け取ったデータを格納する用
				String dataLine;
				String data[];

				// 受け取ったコマンドによって処理を実行
				switch (isKey) {

					// ファイルのデータを表示(1)
					case 1:

						// デバッグ用(サーバに通知)
						System.out.print("# 冷蔵庫の中身データを送信します > ");

						// ファイルデータの読み込み準備
						// 別環境によって起こる文字化け対応(InputStreamReader)
						BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("fridgeData.txt"), "UTF-8"));
						
						// ファイルを一行ずつ読み込む → 最後になるまで実行
						while((dataLine = br.readLine()) != null) {

							// データをTabで区切って格納 → [0]に名前、1に個数、2に日付が入る
							data = (dataLine).split("\t");

							// 現在時刻取得
							LocalDateTime now = LocalDateTime.now();

							// 日付パターン定義(uuuu → 元号や暦が関係しないyyyy)
							DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("uuuu/MM/dd");

							// 文字列日付化
							LocalDateTime limitDate = LocalDate.parse(data[2], dateFormat).atStartOfDay();

							// 日付差分算出
							Duration duration = Duration.between(now, limitDate);

							// 期限を目で見て示す文字列変数
							String square = "", isSafe = "";

							// 期限の日付分だけ四角形を示す
							for (int i = 0; i < duration.toDays() && i <= 14; i++) square += "■";
							
							// 期限切れは×マークを示す
							if (duration.toDays() < 0) isSafe += " ×";

							// 文字列を送信 → 例: 「    卵  6個  2023/01/27  残り18日  ■■■■■■■■■■■■■■■ 」
							out.println(isSafe + "\t" + data[0] + "\t" + data[1] + "個\t" + data[2] + "\t残り" + String.format("%3d", duration.toDays()) + "日 " + square);
						
						}
						out.println("EOF");	// 終了コマンドとして特定文字列送信
						br.close();	// BufferedReaderを閉じる
						System.out.println("送信しました。");	// デバッグ用(サーバに通知)
						break;

					// ファイルのデータに1つ追加(2)
					case 2:
						
						System.out.print("# データを更新します > ");	// デバッグ用

						// ファイルデータの書き込み準備
						// ※別環境によって起こる文字化け対応(OutputStreamWriter)
						PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream("fridgeData.txt", true), "UTF-8")));
						
						name = in.readLine();	// 「商品名」の受信
						items = Integer.parseInt(in.readLine());	// 「個数」の受信
						date = in.readLine();	// 「期限(日付)」の受信
						
						// ファイルの最終行にデータ書き込み
						pw.write(name + "\t" + items + "\t" + date + "\n");

						pw.close();	// PrintWriterを閉じる
						System.out.println("更新しました。");	// デバッグ用(サーバに通知)
						break;
					
					// ファイルのデータを1つ削除(3)
					case 3:
						
						int itemNo = 0;	// 商品の番号管理 & 個数をカウントする用
						String tempData[] = new String[255];	// 一次保存用データ格納

						// ファイルデータの読み込み準備(文字化け対応済み)
						BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream("fridgeData.txt"), "UTF-8"));
						
						// 番号を振って表示
						while ((dataLine = br2.readLine()) != null) {	// ファイルを一行ずつ読み込む → 最後になるまで実行
							tempData[itemNo] = dataLine;	// 1行のデータを配列に一次保存(後に使用)
							data = (dataLine).split("\t");	// データをTabで区切って格納 → [0]に名前、1に個数、2に日付が入る
							out.println(itemNo + "\t" + data[0] + "\t" + data[1] + "個\t" + data[2]);	// 文字列を送信
							itemNo++;	// 商品番号のカウントアップ
						}

						out.println("EOF");	// 終了コマンドとして特定文字列送信
						br2.close();	// BufferedReaderを閉じる

						int delNo = Integer.parseInt(in.readLine());	// 削除する番号を受信
						File tmpFile = new File("tmpData.txt");	// データ消失防止で一時ファイルに書き込み
						tmpFile.createNewFile();	// ファイルを新規作成

						// ファイルデータの書き込み準備(文字化け対応済み)
						PrintWriter tmpPw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream("tmpData.txt"), "UTF-8")));
						
						// 一次保存していた1行ごとの配列データを商品の数だけ一時ファイルへ書き込む
						for (int k = 0; k < itemNo; k++) {
							if (k == delNo) continue;	// 削除する番号のみスルー
							tmpPw.write(tempData[k] + "\n");	// ファイルの最終行へ書き込み
						}
						tmpPw.close();	// PrintWriterを閉じる

						// ファイルパスの定義
						Path tmpPath = Paths.get("tmpData.txt");
						Path dataPath = Paths.get("fridgeData.txt");
						
						// 名前変更による上書き保存でデータ更新(ファイルが既に存在するのでオプションを付加し実行)
						Files.move(tmpPath, dataPath, StandardCopyOption.REPLACE_EXISTING);
						
							// PrintWriterを閉じる
						System.out.println("更新しました。");	// デバッグ用(サーバに通知)
						break;
				}
			}
			
        } catch (IOException e) { // 例外処理
            e.printStackTrace();
        }
    }
}
