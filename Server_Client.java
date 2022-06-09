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
	//Ŭ����� �޼������� ���޹޴� �޼ҵ�
	public void receive() {
		//������ ���� �� ���ʺ� ���� ���
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					while(true) { //Ŭ���̾�Ʈ�κ��� �޼����� ���޹���
						InputStream in = socket.getInputStream();
						byte[]buffer = new byte[512];
						int length = in.read(buffer);
						while(length == -1) throw new IOException();
						System.out.println("[�޼��� ���� ����]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						//���� ������ ���ÿ� �޼����� �ٸ� Ŭ���̾�Ʈ���� ���������ν�
						//Ŭ���̾�Ʈ�ν� ���Ҽ���
						String message = new String(buffer, 0, length, "UTF-8");
						for (Client client : Main.clients) {
							client.send(message);
						}
					}
				}catch(Exception e) {
					try {
						System.out.println("[�޼��� ���� ����]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
					}catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread); // ������pool�� ������� ������(���ʺ�)�� ���
	}
	//Ŭ���̾�Ʈ���� �޼����� �����ϴ� �ޙ�
	//������ �ƿ�ǲ ��Ʈ�� ����Ʈ�� �� �ڹ� �� ���̿�
	public void send(String message) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush(); // �ݵ�� �÷��� �������
					//���� x�� ����Ʈ �Լ� ���ۿ� ��� ������ �������� Ŭ���̾�Ʈ�� ����
				}catch (Exception e) {
					try {
						System.out.println("[�޼��� �۽� ����]" 
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						//Ŭ���̾�Ʈ ���� �߻� ��
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

