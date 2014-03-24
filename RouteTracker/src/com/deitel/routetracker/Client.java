package com.deitel.routetracker;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Client extends JFrame {
	private JTextField interestRateTF = new JTextField();
	private JTextField yearTF = new JTextField();
	private JTextField amountTF = new JTextField();
	private JTextArea resultTA = new JTextArea();
	private JButton submitBTN = new JButton("Submit");
	private Socket socket;
	private DataOutputStream toServer;
	private DataInputStream fromServer;
	private String socketName = "localhost";
	private int port = 8000;

	public static void main(String[] args) {
		new Client();
	}

	public Client() {
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(3, 1));
		p.add(new JLabel("Annual Interest Rate"));
		p.add(new JLabel("Number of Years"));
		p.add(new JLabel("Loan Amount"));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(3,1));
		p1.add(interestRateTF);
		p1.add(yearTF);
		p1.add(amountTF);

		JPanel textFieldsPanel = new JPanel();
		textFieldsPanel.setLayout(new BorderLayout());
		textFieldsPanel.add(p, BorderLayout.WEST);
		textFieldsPanel.add(p1,BorderLayout.CENTER);
		textFieldsPanel.add(submitBTN, BorderLayout.EAST);

		setLayout(new BorderLayout());
		add(textFieldsPanel, BorderLayout.NORTH);
		add(new JScrollPane(resultTA), BorderLayout.CENTER);

		submitBTN.addActionListener(new ButtonListener());

		setTitle("Loan Client");
		setSize(500, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		try {
			// Tell what address and port to send information to
			Socket socket = new Socket(socketName, port);

			fromServer = new DataInputStream(
					socket.getInputStream());

			toServer = new DataOutputStream(
					socket.getOutputStream());
		}
		catch(IOException ex) {
			System.err.println(ex);
		}
	}

	private class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				double interestRate = Double.parseDouble(
						interestRateTF.getText().trim());
				resultTA.append("Interest Rate: " + interestRate + "%\n");
				int year = Integer.parseInt(yearTF.getText().trim());
				resultTA.append("Year: " + year + "\n");
				double loanAmount = Double.parseDouble(amountTF.getText().trim());
				resultTA.append("Loan Amount: $" + loanAmount + "\n");

				toServer.writeDouble(interestRate);
				toServer.flush();
				toServer.writeInt(year);
				toServer.flush();
				toServer.writeDouble(loanAmount);
				toServer.flush();

				double monthlyPayment = fromServer.readDouble();
				resultTA.append("Monthly Payment: $" + monthlyPayment + "\n");
				double totalPayment = fromServer.readDouble();
				resultTA.append("Total Payment: $" + totalPayment + "\n");
			}
			catch(IOException ex) {
				System.err.println(ex);
			}
		}
	}
}

