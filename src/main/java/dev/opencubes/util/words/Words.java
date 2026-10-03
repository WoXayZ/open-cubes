package dev.opencubes.util.words;

import dev.opencubes.util.words.Sequence.Phrase;
import dev.opencubes.util.words.Sequence.Word;

public class Words {

	private static String capitalizeWord(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}
		return Character.toUpperCase(input.charAt(0)) + input.substring(1);
	}

	private static String capitalizeFullyWord(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}
		StringBuilder out = new StringBuilder(input.length());
		boolean cap = true;
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (Character.isWhitespace(c) || c == '-' || c == '\'') {
				out.append(c);
				cap = true;
			} else if (cap) {
				out.append(Character.toUpperCase(c));
				cap = false;
			} else {
				out.append(Character.toLowerCase(c));
			}
		}
		return out.toString();
	}

	private static IGenerator[] convert(Object... args) {
		IGenerator[] result = new IGenerator[args.length];
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (arg instanceof IGenerator) result[i] = (IGenerator)arg;
			else result[i] = terminal(arg);
		}
		return result;
	}

	public static IGenerator terminal(Object object) {
		return new Terminal(object.toString());
	}

	public static IGenerator alt(Object... obj) {
		return new Alternative(convert(obj));
	}

	public static IGenerator seq(Object... obj) {
		return new Phrase(convert(obj));
	}

	public static IGenerator word(Object... obj) {
		return new Word(convert(obj));
	}

	public static IGenerator capitalize(IGenerator gen) {
		return new Transformer(gen) {
			@Override
			protected String transform(String input) {
				return capitalizeWord(input);
			}
		};
	}

	public static IGenerator capitalizeFully(IGenerator gen) {
		return new Transformer(gen) {
			@Override
			protected String transform(String input) {
				return capitalizeFullyWord(input);
			}
		};
	}

	public static IGenerator upper(IGenerator gen) {
		return new Transformer(gen) {
			@Override
			protected String transform(String input) {
				return input.toUpperCase();
			}
		};
	}

	public static IGenerator lower(IGenerator gen) {
		return new Transformer(gen) {
			@Override
			protected String transform(String input) {
				return input.toLowerCase();
			}
		};
	}

	public static IGenerator opt(float probability, IGenerator gen) {
		return new Optional(gen, probability);
	}

	public static IGenerator sub(String key) {
		return new Substitution(key, "");
	}

	public static IGenerator sub(String key, String defaultValue) {
		return new Substitution(key, defaultValue);
	}

	public static IGenerator range(int start, int end) {
		return new Range(start, end);
	}
}
