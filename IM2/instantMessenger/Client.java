package instantMessenger;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

import javax.swing.*;

public class Client extends JFrame {

	private JPanel p = new JPanel();
	private JTextField textBox = new JTextField();
	private JTextArea displayBox = new JTextArea();
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ObjectOutputStream outputServer;
	private ObjectInputStream inputServer;
	private ServerSocket miniServer;
	private Socket connection;
	private Socket connectionServer;
	private String IPToConnectTo = "";
	private String serverIP = "192.168.1.6";
	private int localPort = 0;
	private JButton connectButton = new JButton("Connect To");
	protected static ArrayList<String> addresses = new ArrayList<String>();

	public Client() {
		super("Instant Messenger");

		textBox.setEditable(false);
		displayBox.setEditable(false);
		displayBox.setText("Welcome!\n");

		textBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (IPToConnectTo != null)
					sendMessage(e.getActionCommand());
				else
					IPToConnectTo = e.getActionCommand();
				textBox.setText("");
			}
		});

		changeConnectButton(false);
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeConnectButton(false);
				IPToConnectTo = JOptionPane.showInputDialog("Enter IP Address");
				changeConnectButton(true);

				Thread connectToServer = new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub

						if (IPToConnectTo != null) {
							IPToConnectTo = IPToConnectTo.trim();
							if (IPToConnectTo != " " & IPToConnectTo != "") {
								try {
									outputServer
											.writeObject((String) IPToConnectTo);
									outputServer.flush();
								} catch (SocketException s) {
									closeConnectionServer();
									handleLostConnectionServer();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								try {
									final Integer portToFind = (Integer) inputServer
											.readObject();
									if (portToFind == null)
										JOptionPane.showMessageDialog(null,
												IPToConnectTo + " is offline");
									else {
										new Thread(new Runnable() {

											@Override
											public void run() {
												if (alreadyConnectedTo(IPToConnectTo))
													JOptionPane
															.showMessageDialog(
																	null,
																	"You're already connected to "
																			+ IPToConnectTo);
												else {
													addresses
															.add(IPToConnectTo);
													new Outgoing(IPToConnectTo,
															portToFind);
												}
											}
										}).start();
									}
								} catch (SocketException s) {
									closeConnectionServer();
									handleLostConnectionServer();
								} catch (ClassNotFoundException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}

							}
						}
					}

				});
				connectToServer.start();
			}

		});

		add(p, BorderLayout.NORTH);
		add(new JScrollPane(textBox), BorderLayout.SOUTH);
		add(new JScrollPane(displayBox), BorderLayout.CENTER);
		p.setLayout(new FlowLayout());
		p.add(connectButton);
		setLocation(200, 100);
		setSize(300, 500);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		try {
			miniServer = new ServerSocket();
			miniServer.setReuseAddress(true);
			miniServer.bind(new InetSocketAddress(localPort));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		while (!alertServer()) {// alert the server and keep doing it if it
								// fails
		}
		// if true is passed in it means the client is telling the server that
		// he is online
		// if false is passed in it means the clients wants the server to look
		// up the port of someone

		changeConnectButton(true);

		waitForConnection();// after alerting server, start waiting for a
							// connection

	}

	private void handleLostConnectionServer() {
		JOptionPane
				.showMessageDialog(null,
						"Server Seems To Be Offline, We're Going To Attempt To Reconect");
		changeConnectButton(false);
		while (!alertServer()) {
		}
		changeConnectButton(true);
		JOptionPane
				.showMessageDialog(null,
						"Yay! Server is Back Online! You Should Be Able To Connect To Other Users Now");

	}

	private void closeConnectionServer() {
		// TODO Auto-generated method stub
		Thread close = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					inputServer.close();
					outputServer.close();
					connectionServer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		close.start();
		// Client.addresses.remove(otherUserIP);
	}

	private boolean alreadyConnectedTo(String iPToConnectTo) {
		for (String ip : addresses) {
			if (ip.equals(iPToConnectTo))
				return true;
		}
		return false;
	}

	private boolean alertServer() {// the parameter is true or
									// false....this tells the server
									// the reason for connecting
		// TODO Auto-generated method stub
		try {
			try {
				displayMessage("Connecting to Server...");
				// miniServer = new ServerSocket(localPort, 100);//create a
				// server socket
				localPort = miniServer.getLocalPort();
				connectionServer = new Socket(serverIP, 6789);
				outputServer = new ObjectOutputStream(
						connectionServer.getOutputStream());
				displayMessage("Waiting For Server To Respond");
				inputServer = new ObjectInputStream(
						connectionServer.getInputStream());
				displayMessage("Server is online");
				outputServer.writeObject(localPort);
			} catch (ConnectException e) {
				displayBox.setText("");
				displayMessage("Connection Timed Out, Retrying...");

				return false;
			}

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			displayMessage("Server Cannot Be Reached");
			// e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	private void waitForConnection() {
		// TODO Auto-generated method stub
		Thread waitForConnection = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					try {
						connection = miniServer.accept();
						addresses.add(connection.getInetAddress()
								.getHostAddress().trim());
						new Incoming(connection, input, output);
						waitForConnection();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		waitForConnection.start();
	}

	private void changeConnectButton(final boolean b) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				connectButton.setEnabled(b);
			}

		});
	}

	private void newLine(String message) {
		// TODO Auto-generated method stub
		for (int i = 0; i < message.length(); i++) {
			if (i % 40 == 0)
				message = message.substring(0, i) + "\n" + message.substring(i);
		}
	}

	private void displayMessage(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				newLine(message);
				displayBox.append(message + "\n");
			}
		});
	}

	private void sendMessage(String message) {
		try {
			output.writeObject(message);
			displayMessage("Client: " + message);
			output.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Client c = new Client();
	}

}
