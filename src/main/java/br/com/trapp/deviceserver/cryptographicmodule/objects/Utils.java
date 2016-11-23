package br.com.trapp.deviceserver.cryptographicmodule.objects;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.LayoutStyle;

public class Utils {

    public static char[] getPassword() {
	JPanel panel = new JPanel();
	GroupLayout layout = new GroupLayout(panel);
	panel.setLayout(layout);
	layout.setAutoCreateGaps(true);
	layout.setAutoCreateContainerGaps(true);
	JLabel explainText = new JLabel(
		"<html> <br>Se você deseja autorizar a assinatura<br> insira o PIN do seu dispositivo para que<br> a assinatura se complete. <br>Senão, pressione Cancelar<html>",
		JLabel.CENTER);
	JLabel label = new JLabel("PIN:");
	JPasswordField pass = new JPasswordField(10);
	layout.setHorizontalGroup(
		layout.createSequentialGroup().addGroup(layout.createParallelGroup().addComponent(explainText)
			.addGroup(layout.createSequentialGroup().addComponent(label).addComponent(pass))));
	layout.setVerticalGroup(layout.createSequentialGroup().addComponent(explainText)
		.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(label)
			.addComponent(pass)));

	Icon icon = new ImageIcon("/home/marlon/icon.png");
	String[] options = new String[] { "OK", "Cancelar" };
	int option = JOptionPane.showOptionDialog(null, panel, "Módulo criptográfico", JOptionPane.NO_OPTION,
		JOptionPane.PLAIN_MESSAGE, icon, options, options[0]);
	char[] password = null;
	if (option == 0) // pressing OK button
	{
	    password = pass.getPassword();
	}
	return password;
    }

}
