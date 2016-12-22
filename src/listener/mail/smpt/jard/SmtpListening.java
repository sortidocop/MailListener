package listener.mail.smpt.jard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class SmtpListening {

	public static void main(String[] param) throws Exception {
		@SuppressWarnings("resource")
		final ServerSocket localServerSocket = new ServerSocket(25);
		try {
			while (true) {
				Socket localSocket = localServerSocket.accept();
				System.out.println("Start with " + localSocket.getInetAddress());

				SmtpListening.listenSmtp localSession = new SmtpListening.listenSmtp(localSocket);
				Thread localThread = new Thread(localSession);
				localThread.setDaemon(true);
				localThread.start();
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	private static class listenSmtp implements Runnable {
		private final Socket client;
		private final BufferedReader reader;
		private final PrintWriter writer;

		public listenSmtp(Socket paramSocket) throws IOException {
			this.client = paramSocket;
			this.reader = new BufferedReader(new InputStreamReader(paramSocket.getInputStream(), "US-ASCII"));

			this.writer = new PrintWriter(new OutputStreamWriter(paramSocket.getOutputStream(), "US-ASCII"), true);
		}

		@Override
		public void run() {

			try {

				writer.print("220  ESMTP testmail; ");
				synchronized (SmtpListening.class) {
					writer.println(new Date());
				}

				String s1 = null;
				while ((s1 = reader.readLine()) != null && !s1.toUpperCase().startsWith("QUIT")
						&& !s1.toUpperCase().startsWith("DATA")) {
					writer.println("250 OK");
				}

				if (s1 == null) {
					// il n'y a pas de data.
					return;
				}

				if (s1.toUpperCase().startsWith("QUIT")) {
					writer.println("221 OK");
					return;
				}

				writer.println("354 OK");

				while ((s1 = reader.readLine()) != null && !s1.equals(".")) {
					System.out.println(s1);
				}

				writer.println("250 OK");

			} catch (Exception localException) {
				localException.printStackTrace();
			} finally {
				// System.out.println("Close with " +
				// this.client.getInetAddress());
				try {
					this.reader.close();
				} catch (IOException localIOException1) {
				}
				this.writer.flush();
				this.writer.close();
				try {
					this.client.close();
				} catch (IOException localIOException2) {
					System.err.println("Problem question");
				}
			}

		}

	}
}
