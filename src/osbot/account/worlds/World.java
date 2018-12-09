package osbot.account.worlds;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import osbot.random.RandomUtil;

public class World {

	private static final long serialVersionUID = -9046100616950752889L;

	private static List<World> worlds;

	private static Comparator<World> worldComparator = (w1, w2) -> {
		int typeComparison = w1.getType().compareTo(w2.getType());

		if (typeComparison != 0) {
			return typeComparison;
		}

		return Integer.compare(w1.getPlayerAmount(), w2.getPlayerAmount());
	};

	private WorldType type;
	private int number;
	private String detail;
	private int playerAmount;

	public World(final WorldType type, final int number, final String detail, final int playerAmount) {
		this.type = type;
		this.number = number;
		this.detail = detail;
		this.setPlayerAmount(playerAmount);
	}

	public final WorldType getType() {
		return type;
	}

	public final int getNumber() {
		return number;
	}

	public final String getDetail() {
		return detail;
	}

	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.writeObject(getType());
		stream.writeInt(getNumber());
		stream.writeObject(getDetail());
	}

	private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
		type = (WorldType) stream.readObject();
		number = stream.readInt();
		detail = (String) stream.readObject();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof World)) {
			return false;
		}

		World otherWorld = (World) other;

		return getType() == otherWorld.getType() && getNumber() == otherWorld.getNumber();
	}

	@Override
	public String toString() {
		String worldStr = getType().toString() + " " + getNumber();

		if (!getDetail().isEmpty()) {
			worldStr += " - " + getDetail();
		}

		return worldStr;
	}

	public static List<World> getWorlds() {
		if (worlds == null) {
			loadWorlds();
		}
		return worlds;
	}

	public static World getRandomWorldWithLessPopulation(WorldType type, int minAmountOfWorlds) {
		return getWorldsWithoutTotalRequirements(type).get(RandomUtil.getRandomNumberInRange(0,
				getWorldsWithoutTotalRequirements(type).size() < minAmountOfWorlds
						? getWorldsWithoutTotalRequirements(type).size()
						: minAmountOfWorlds));
	}

	public static List<World> getWorldsWithoutTotalRequirements(WorldType type) {
		if (worlds == null) {
			loadWorlds();
		}
		return worlds.stream().filter(world -> world.getType() == type && !world.getDetail().contains("skill total"))
				.collect(Collectors.toList());
	}

	public static Comparator<World> getWorldComparator() {
		return worldComparator;
	}

	private static void loadWorlds() {
		worlds = new ArrayList<>();

		try {
			Document doc = Jsoup.connect("http://oldschool.runescape.com/slu").get();
			Elements servers = doc.select("tr.server-list__row");
			for (Element server : servers) {
				Element serverLink = server.selectFirst(".server-list__world-link");
				String worldIDStr = serverLink.id().replaceAll("slu-world-", "");
				int worldNum = Integer.parseInt(worldIDStr);

				Element membershipType = server.selectFirst(".server-list__row-cell--type");
				boolean members = membershipType.html().equals("Members");
				WorldType worldType = members ? WorldType.MEMBERS : WorldType.F2P;

				String worldDetail = membershipType.nextElementSibling().ownText();

				Elements playerAmountElement = server.select(".server-list__row-cell");
				int playerAmount = Integer.parseInt(playerAmountElement.get(1).toString().replaceAll("[\\D]", ""));

				if (worldDetail.equals("-")) {
					worldDetail = "";
				}

				worlds.add(new World(worldType, worldNum, worldDetail, playerAmount));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		worlds.sort(worldComparator);
	}

	/**
	 * @return the playerAmount
	 */
	public int getPlayerAmount() {
		return playerAmount;
	}

	/**
	 * @param playerAmount
	 *            the playerAmount to set
	 */
	public void setPlayerAmount(int playerAmount) {
		this.playerAmount = playerAmount;
	}

}
