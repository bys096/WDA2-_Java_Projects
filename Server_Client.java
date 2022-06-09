package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
	Socket socket;
	
	public Client(Socket socket) {
		this.socket = socket;
		receive();
	}
	//클라부터 메세지르르 전달받는 메소드
	public void receive() {
		//쓰레드 만들 때 러너블 많이 사용
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					while(true) { //클라이언트로부터 메세지를 전달받음
						InputStream in = socket.getInputStream();
						byte[]buffer = new byte[512];
						int length = in.read(buffer);
						while(length == -1) throw new IOException();
						System.out.println("[메세지 수신 성공]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						//전달 받음과 동시에 메세지는 다른 클라이언트에게 전달함으로써
						//클라이언트로써 역할수행
						String message = new String(buffer, 0, length, "UTF-8");
						for (Client client : Main.clients) {
							client.send(message);
						}
					}
				}catch(Exception e) {
					try {
						System.out.println("[메세지 수신 오류]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
					}catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread); // 쓰레드pool에 만들어진 쓰레드(러너블)를 등록
	}
	//클라이언트에게 메세지를 전송하는 메솓
	//전송은 아웃풋 스트림 임폴트는 꼭 자바 점 아이오
	public void send(String message) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush(); // 반드시 플러쉬 해줘야함
					//오류 x시 라이트 함수 버퍼에 담긴 내용을 서버에서 클라이언트로 전송
				}catch (Exception e) {
					try {
						System.out.println("[메세지 송신 오류]" 
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						//클라이언트 오류 발생 시
						Main.clients.remove(Client.this);
						socket.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread);
	}
}

