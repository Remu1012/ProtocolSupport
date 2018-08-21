package protocolsupport.protocol.utils.i18n;

import java.io.BufferedReader;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import protocolsupport.utils.Utils;

public class I18NData {

	protected static final String resource_path = "i18n/";

	public static final String DEFAULT_LOCALE = "en_us";

	//the languages file is generated by the gradle build script
	protected static final Set<String> builtInLocales = Utils.getResource(resource_path + "languages").lines().collect(Collectors.toSet());
	protected static final ConcurrentMap<String, I18N> i18ns = new ConcurrentHashMap<>();
	protected static final I18N defaultI18N = loadBuiltInI18N(DEFAULT_LOCALE);

	public static Set<String> getBuiltInLocales() {
		return Collections.unmodifiableSet(builtInLocales);
	}

	public static boolean isI18NLoaded(String locale) {
		return i18ns.containsKey(locale);
	}

	public static I18N loadBuiltInI18N(String locale) {
		if (!builtInLocales.contains(locale)) {
			throw new IllegalArgumentException(MessageFormat.format("{0} is not a built-in locale", locale));
		}
		return loadAndRegisterI18N(locale, Utils.getResource(resource_path + locale + ".json"));
	}

	public static I18N loadAndRegisterI18N(String locale, BufferedReader stream) {
		I18N i18n = new I18N(
			locale,
			Utils.GSON.fromJson(stream, JsonObject.class).entrySet().stream()
			.collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getAsString()))
		);
		i18ns.put(locale, i18n);
		return i18n;
	}

	public static void unloadI18N(String locale) {
		i18ns.remove(locale);
	}

	public static I18N getI18N(String locale) {
		return i18ns.getOrDefault(locale, defaultI18N);
	}

	public static String getTranslationString(String locale, String key) {
		String tlstring = getI18N(locale).getTranslationString(key);
		if (tlstring != null) {
			return tlstring;
		}
		String deftlstring = defaultI18N.getTranslationString(key);
		if (deftlstring != null) {
			return deftlstring;
		}
		return MessageFormat.format("Unknown translation key: {0}", key);
	}

	public static String translate(String locale, String key, String... args) {
		return String.format(getTranslationString(locale, key), (Object[]) args);
	}

}
