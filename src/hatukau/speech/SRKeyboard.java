package hatukau.speech;

import android.inputmethodservice.Keyboard;
import android.view.KeyEvent;

class MultipleEraseInfo {
	int times = 0;

}

public class SRKeyboard {

	static int SELECT_ALL = -9;
	static int ERASE_ALL = -10;
	static int COPY = -11;
	static int PASTE = -12;
	static int COPY_ALL = -13;
	static int UNDO = -14;

	static void d(String s) {
		System.out.println("SRKeyboard " + s);
	}

	static MultipleEraseInfo getMultipleEraseCommand(String[] toks) {

		try {
			for (int i = 0; i < toks.length; i++) {
				if (toks[i].indexOf("ERASE") >= 0) {
					if (toks[i + 1].indexOf("NUM_") >= 0
							& toks[i + 2].indexOf("TIMES") >= 0) {

						MultipleEraseInfo info = new MultipleEraseInfo();
						byte bs = (byte) getChar(toks[i + 1]);
						String ts = new String(new byte[] { bs });
						d("ts " + ts);
						info.times = Integer.valueOf(ts);
						return info;

					}
				}
			}

		} catch (Exception ex) {

		}

		return null;
	}

	static int getCommand(String[] toks) {

		if (toks.length >= 2) {

			for (int i = 0; i < toks.length; i++) {
				if (toks[i].equalsIgnoreCase("SELECT"))
					if (toks[i + 1].equalsIgnoreCase("ALL")) {

						return SELECT_ALL;
					}
			}

			for (int i = 0; i < toks.length; i++) {
				if (toks[i].equalsIgnoreCase("UNDO"))
						return UNDO;
			}

			for (int i = 0; i < toks.length; i++) {
				if (toks[i].equalsIgnoreCase("ERASE_ALL"))
						return ERASE_ALL;
			}
			
			for (int i = 0; i < toks.length; i++) {
				if (toks[i].equalsIgnoreCase("ERASE"))
					if (toks[i + 1].equalsIgnoreCase("ALL")) {

						return ERASE_ALL;
					}
			}

			for (int i = 0; i < toks.length; i++) {
				if (toks[i].equalsIgnoreCase("COPY"))
					if (toks[i + 1].equalsIgnoreCase("ALL")) {

						return COPY_ALL;
					}
			}

			// single commands ?
			// MUST after multiple check above !!!

			for (int i = 0; i < toks.length; i++) {
				if (toks[i].equalsIgnoreCase("COPY"))
					return COPY;
			}

			for (int i = 0; i < toks.length; i++) {
				if (toks[i].equalsIgnoreCase("PASTE"))
					return PASTE;
			}

		}

		return 0;
	}

	static int getChar(String tok) {

		if (tok != null) {

			if (tok.equalsIgnoreCase("NOI") | tok.equalsIgnoreCase("SIL"))
				return 0;

			if (tok.equalsIgnoreCase("CAPITAL"))
				return Keyboard.KEYCODE_SHIFT;

			if (tok.equalsIgnoreCase("SPACE"))
				return ' ';

			if (tok.equalsIgnoreCase("ENTER"))
				return Keyboard.KEYCODE_DONE;

			if (tok.equalsIgnoreCase("NUM_1"))
				return '1';
			if (tok.equalsIgnoreCase("NUM_2"))
				return '2';
			if (tok.equalsIgnoreCase("NUM_3"))
				return '3';
			if (tok.equalsIgnoreCase("NUM_4"))
				return '4';
			if (tok.equalsIgnoreCase("NUM_5"))
				return '5';
			if (tok.equalsIgnoreCase("NUM_6"))
				return '6';
			if (tok.equalsIgnoreCase("NUM_7"))
				return '7';
			if (tok.equalsIgnoreCase("NUM_8"))
				return '8';
			if (tok.equalsIgnoreCase("NUM_9"))
				return '9';
			if (tok.equalsIgnoreCase("NUM_0"))
				return '0';

			// for 26 letter
			if (tok.length() > 0) {
				char[] bufc = new char[1];
				tok.getChars(0, 1, bufc, 0);
				return bufc[0];
			}

		}
		return 0;
	}

	static int getCommand(String tok) {

		if (tok != null) {
			if (tok.equalsIgnoreCase("ERASE"))
				return Keyboard.KEYCODE_DELETE;
			if (tok.equalsIgnoreCase("ENTER"))
				return Keyboard.KEYCODE_DONE;
			if (tok.equalsIgnoreCase("ENTER"))
				return Keyboard.KEYCODE_SHIFT;

		}

		return 0;
	}
}
