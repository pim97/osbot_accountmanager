package osbot.account.creator;

import java.util.HashSet;
import java.util.Set;

import com.github.javafaker.Faker;

public class RandomNameGenerator {

	public String generateRandomNameString() {
		StringBuilder name = new StringBuilder();
		String setName = generateRandomName();
		String randomIdentitier = randomIdentifier();
		randomIdentitier = randomIdentitier.substring(0, randomIdentitier.length() < 8 ? randomIdentitier.length() : 8);
		setName = setName.substring(0, setName.length() < 4 ? setName.length() : 4);

		name.append(setName);
		name.append(randomIdentitier);

		// Max length of names is 12
		if (name.length() > 12) {
			name.setLength(12);
		}
		return name.toString().toLowerCase().replaceAll("[^a-zA-Z0-9]+", "");
	}

	private String generateRandomName() {
		Faker fake = new Faker();
		int random = (int) (Math.random() * 100);

		if (random > 0 && random < 20) {
			return fake.leagueOfLegends().masteries();
		} else if (random >= 20 && random < 40) {
			return fake.currency().name();
		} else if (random >= 40 && random < 60) {
			return fake.company().name();
		} else if (random >= 60 && random < 80) {
			return fake.esports().team();
		} else if (random >= 80 && random <= 100) {
			return fake.pokemon().name();
		} else {
			return fake.leagueOfLegends().champion();
		}
	}

	// class variable
	final String lexicon = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345674890";

	final java.util.Random rand = new java.util.Random();

	// consider using a Map<String,Boolean> to say whether the identifier is being
	// used or not
	final Set<String> identifiers = new HashSet<String>();

	private String randomIdentifier() {
		StringBuilder builder = new StringBuilder();
		while (builder.toString().length() == 0) {
			int length = 50; // rand.nextInt(5) + 7;
			for (int i = 0; i < length; i++) {
				builder.append(lexicon.charAt(rand.nextInt(lexicon.length())));
			}
			if (identifiers.contains(builder.toString())) {
				builder = new StringBuilder();
			}
		}
		return builder.toString();
	}
}
