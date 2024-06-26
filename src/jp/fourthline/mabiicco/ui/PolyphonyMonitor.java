/*
 * Copyright (C) 2024 たんらる
 */
package jp.fourthline.mabiicco.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jp.fourthline.mabiicco.ActionDispatcher;
import jp.fourthline.mabiicco.AppResource;
import jp.fourthline.mabiicco.midi.MabiDLS;

public final class PolyphonyMonitor implements Runnable {
	private static final int BAR_W = 3;
	private static final int MAX_V = 256;
	private static final int M_HEIGHT = 80;

	private int value = 0;
	private int max = 0;
	private final JDialog dialog;
	private final JTextField textField = new JTextField(9);
	private final JPanel mainPanel;

	private static PolyphonyMonitor instance = null;
	public static PolyphonyMonitor getInstance() {
		if (instance == null) {
			instance = new PolyphonyMonitor();
			ActionDispatcher.getInstance().addUpdateUIComponent(instance.dialog);
			new Thread(instance, "PolyphonyMonitor").start();
		}
		return instance;
	}

	private PolyphonyMonitor() {
		dialog = new JDialog();
		dialog.setTitle(AppResource.appText("menu.polyphonyMonitor"));
		dialog.setResizable(false);
		dialog.setAlwaysOnTop(true);

		JButton clearButton = new JButton("Clear");
		clearButton.setFocusable(false);
		clearButton.addActionListener(t -> reset());
		JButton closeButton = new JButton("Close");
		closeButton.setFocusable(false);
		closeButton.addActionListener(t -> hide());

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(clearButton);
		buttonPanel.add(closeButton);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(mainPanel = createMainPanel(), BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		dialog.add(panel);
		dialog.pack();

		textField.setEditable(false);
		textField.setFocusable(false);
		textField.setFont(AppResource.MONO_FONT);
		setValue(0);
	}

	public JPanel createMainPanel() {
		var p = new JPanel() {
			private static final long serialVersionUID = -7206453832110132604L;

			private int x(int v) {
				return v* BAR_W + 16;
			}

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D)g.create();
				UIUtils.setRenderingHint(g2);

				g2.setColor(getForeground());
				for (int i = 0; i <= MAX_V; i += 32) {
					int x = x(i) - 1;
					g2.drawLine(x, 16, x, M_HEIGHT);
					g2.drawString(Integer.toString(i), x-2, 12);
				}

				g2.setColor(new Color(0, 128, 0));
				for (int i = 1; i <= value; i++) {
					int x = x(i);
					g2.fillRect(x, 24, BAR_W / 2, M_HEIGHT-24);
				}

				g2.setColor(Color.RED);
				if (max > 0) {
					int x = x(max);
					g2.fillRect(x, 24, 1, M_HEIGHT-24);
					g2.drawString(Integer.toString(max), x+4, 32);
				}
			}
		};

		p.setPreferredSize(new Dimension(MAX_V * BAR_W + 64, M_HEIGHT));
		return p;
	}

	public JTextField getTextField() {
		return textField;
	}

	private void setValue(int v) {
		this.value = v;
		max = Math.max(max, v);
		StringBuilder sb = new StringBuilder();
		sb.append("Poly:");
		if (v < 10) {
			sb.append("  ");
		} else if (v < 100) {
			sb.append(" ");
		}
		sb.append(v);
		sb.append("    ");
		var s = sb.toString();
		if (!s.equals(textField.getText())) {
			textField.setText(s);
			mainPanel.repaint();
		}
	}

	public void update() {
		var status = MabiDLS.getInstance().getSynthesizer().getVoiceStatus();
		int count = 0;
		for (var i : status) {
			if ((i.active) && (i.volume > 0)) count++;
		}
		setValue(count);
	}

	public void show(JFrame parent) {
		if (!dialog.isVisible()) {
			dialog.setLocationRelativeTo(parent);
			dialog.setVisible(true);
		}
	}

	public void hide() {
		dialog.setVisible(false);
	}

	private void reset() {
		max = 0;
		mainPanel.repaint();
	}

	@Override
	public void run() {
		while (true) {
			try {
				update();
				Thread.sleep(100);
			} catch (Exception e) {}
		}
	}
}
