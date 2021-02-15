package trading.economy;

import java.util.*;

//TODO:

/**Class representing an unusual item's effect. This class cannot be directly constructed, instead the forName or forInt methods must be used.<br>
References to an effect's int value refer to its Steam API schema integer value. For more information, see https://backpack.tf/developer/particles
*/

public class Effect{
	private final String name;
	private final int code;

	private static final Set<Effect> allEffects = Set.of(
		new Effect("Community Sparkle", 4), 
        new Effect("Holy Glow", 5), 
        new Effect("Green Confetti", 6), 
        new Effect("Purple Confetti", 7), 
        new Effect("Haunted Ghosts", 8), 
        new Effect("Green Energy", 9), 
        new Effect("Purple Energy", 10), 
        new Effect("Circling TF Logo", 11), 
        new Effect("Massed Flies", 12), 
        new Effect("Burning Flames", 13), 
        new Effect("Scorching Flames", 14), 
        new Effect("Sunbeams", 17), 
        new Effect("Map Stamps", 20), 
        new Effect("Stormy Storm", 29), 
        new Effect("Orbiting Fire", 33), 
        new Effect("Bubbling", 34),
        new Effect("Smoking", 35),
        new Effect("Steaming", 36), 
        new Effect("Cloudy Moon", 38), 
        new Effect("Kill-a-Watt", 56), 
        new Effect("Terror-Watt", 57), 
        new Effect("Cloud 9", 58), 
        new Effect("Time Warp", 70), 
        new Effect("Searing Plasma", 15), 
        new Effect("Vivid Plasma", 16), 
        new Effect("Circling Peace Sign", 18), 
        new Effect("Circling Heart", 19), 
        new Effect("Blizzardy Storm", 30), 
        new Effect("Nuts n' Bolts", 31), 
        new Effect("Orbiting Planets", 32), 
        new Effect("Flaming Lantern", 37), 
        new Effect("Cauldron Bubbles", 39), 
        new Effect("Eerie Orbiting Fire", 40), 
        new Effect("Knifestorm", 43), 
        new Effect("Misty Skull", 44), 
        new Effect("Harvest Moon", 45), 
        new Effect("It's A Secret To Everybody", 46), 
        new Effect("Stormy 13th Hour", 47), 
        new Effect("Aces High", 59), 
        new Effect("Dead Presidents", 60), 
        new Effect("Miami Nights", 61),
        new Effect("Disco Beat Down", 62),
        new Effect("Phosphorous", 63),
        new Effect("Sulphurous", 64),
        new Effect("Memory Leak", 65),
        new Effect("Overclocked", 66),
        new Effect("Electrostatic", 67),
        new Effect("Power Surge", 68),
        new Effect("Anti-Freeze", 69),
        new Effect("Green Black Hole", 71),
        new Effect("Roboactive", 72),
        new Effect("Arcana", 73),
        new Effect("Spellbound", 74),
        new Effect("Chiroptera Venenata", 75),
        new Effect("Poisoned Shadows", 76),
        new Effect("Something Burning This Way Comes", 77),
        new Effect("Hellfire", 78),
        new Effect("Darkblaze", 79),
        new Effect("Demonflame", 80),
        new Effect("Showstopper", 3001),
        new Effect("Holy Grail", 3003),
        new Effect("'72", 3004),
        new Effect("Fountain of Delight", 3005),
        new Effect("Screaming Tiger", 3006),
        new Effect("Skill Gotten Gains", 3007),
        new Effect("Midnight Whirlwind", 3008),
        new Effect("Silver Cyclone", 3009),
        new Effect("Mega Strike", 3010),
        new Effect("Bonzo The All-Gnawing", 81),
        new Effect("Amaranthine", 82),
        new Effect("Stare From Beyond", 83),
        new Effect("The Ooze", 84),
        new Effect("Ghastly Ghosts Jr", 85),
        new Effect("Haunted Phantasm Jr", 86),
        new Effect("Haunted Phantasm", 3011),
        new Effect("Ghastly Ghosts", 3012),
        new Effect("Frostbite", 87),
        new Effect("Molten Mallard", 88),
        new Effect("Morning Glory", 89),
        new Effect("Death at Dusk", 90),
        new Effect("Hot", 701),
        new Effect("Isotope", 702),
        new Effect("Cool", 703),
        new Effect("Energy Orb", 704),
        new Effect("Abduction", 91),
        new Effect("Atomic", 92),
        new Effect("Subatomic", 93),
        new Effect("Electric Hat Protector", 94),
        new Effect("Magnetic Hat Protector", 95),
        new Effect("Voltaic Hat Protector", 96),
        new Effect("Galactic Codex", 97),
        new Effect("Ancient Codex", 98),
        new Effect("Nebula", 99),
        new Effect("Death by Disco", 100),
        new Effect("It's a mystery to everyone", 101),
        new Effect("It's a puzzle to me", 102),
        new Effect("Ether Trail", 103),
        new Effect("Nether Trail", 104),
        new Effect("Ancient Eldritch", 105),
        new Effect("Eldritch Flame", 106),
        new Effect("Tesla Coil", 108),
        new Effect("Neutron Star", 107),
        new Effect("Starstorm Insomnia", 109),
        new Effect("Starstorm Slumber", 110),
        new Effect("Infernal Flames", 3015),
        new Effect("Hellish Inferno", 3013),
        new Effect("Spectral Swirl", 3014),
        new Effect("Infernal Smoke", 3016),
        new Effect("Brain Drain", 111),
    	new Effect("Open Mind", 112),
    	new Effect("Head of Steam", 113),
    	new Effect("Galactic Gateway", 114),
    	new Effect("The Eldritch Opening", 115),
    	new Effect("The Dark Doorway", 116),
    	new Effect("Ring of Fire", 117),
    	new Effect("Vicious Circle", 118),
    	new Effect("White Lightning", 119),
    	new Effect("Omniscient Orb", 120),
    	new Effect("Clairvoyance", 121),
    	new Effect("Acidic Bubbles of Envy", 3017),
    	new Effect("Flammable Bubbles of Attraction", 3018),
    	new Effect("Poisonous Bubbles of Regret", 3019),
    	new Effect("Roaring Rockets", 3020),
    	new Effect("Spooky Night", 3021),
    	new Effect("Ominous Night", 3022),
    	new Effect("Fifth Dimension", 122),
    	new Effect("Vicious Vortex", 123),
    	new Effect("Menacing Miasma", 124),
    	new Effect("Abyssal Aura", 125),
    	new Effect("Wicked Wood", 126),
    	new Effect("Ghastly Grove", 127),
    	new Effect("Mystical Medley", 128),
    	new Effect("Ethereal Essence", 129),
    	new Effect("Twisted Radiance", 130),
    	new Effect("Violet Vortex", 131),
    	new Effect("Verdant Vortex", 132),
    	new Effect("Valiant Vortex", 133),
    	new Effect("Bewitched", 3023),
    	new Effect("Accursed", 3024),
    	new Effect("Enchanted", 3025),
    	new Effect("Static Mist", 3026),
    	new Effect("Eerie Lightning", 3027),
    	new Effect("Terrifying Thunder", 3028),
    	new Effect("Jarate Shock", 3029),
    	new Effect("Nether Void", 3030),
    	new Effect("Sparkling Lights", 134),
    	new Effect("Frozen Icefall", 135),
    	new Effect("Fragmented Gluons", 136),
    	new Effect("Fragmented Quarks", 137),
    	new Effect("Fragmented Photons", 138),
    	new Effect("Defragmenting Reality", 139),
    	new Effect("Fragmenting Reality", 141),
    	new Effect("Refragmenting Reality", 142),
    	new Effect("Snowfallen", 143),
    	new Effect("Snowblinded", 144),
    	new Effect("Pyroland Daydream", 145),
    	new Effect("Good-Hearted Goodies", 3031),
    	new Effect("Wintery Wisp", 3032),
    	new Effect("Arctic Aurora", 3033),
    	new Effect("Winter Spirit", 3034),
    	new Effect("Festive Spirit", 3035),
    	new Effect("Magical Spirit", 3036),
    	new Effect("Verdatica", 147),
    	new Effect("Aromatica", 148),
    	new Effect("Chromatica", 149),
    	new Effect("Prismatica", 150),
    	new Effect("Bee Swarm", 151),
    	new Effect("Frisky Fireflies", 152),
    	new Effect("Smoldering Spirits", 153),
    	new Effect("Wandering Wisps", 154),
    	new Effect("Kaleidoscope", 155),
    	new Effect("Green Giggler", 156),
    	new Effect("Laugh-O-Lantern", 157),
    	new Effect("Plum Prankster", 158),
    	new Effect("Pyroland Nightmare", 159),
    	new Effect("Gravelly Ghoul", 160),
    	new Effect("Vexed Volcanics", 161),
    	new Effect("Gourdian Angel", 162),
    	new Effect("Pumpkin Party", 163),
    	new Effect("Spectral Escort", 3037),
    	new Effect("Astral Presence", 3038),
    	new Effect("Arcane Assistance", 3039),
    	new Effect("Arcane Assistance", 3040),
    	new Effect("Emerald Allurement", 3041),
    	new Effect("Pyrophoric Personality", 3042),
    	new Effect("Spellbound Aspect", 3043),
    	new Effect("Static Shock", 3044),
    	new Effect("Veno Shock", 3045),
    	new Effect("Toxic Terrors", 3046),
    	new Effect("Arachnid Assault", 3047),
    	new Effect("Creepy Crawlies", 3048),
    	new Effect("Frozen Fractals", 164),
    	new Effect("Lavender Landfall", 165),
    	new Effect("Special Snowfall", 166),
    	new Effect("Divine Desire", 167),
    	new Effect("Distant Dream", 168),
    	new Effect("Violent Wintertide", 169),
    	new Effect("Blighted Snowstorm", 170),
    	new Effect("Pale Nimbus", 171),
    	new Effect("Genus Plasmos", 172),
    	new Effect("Serenus Lumen", 173),
    	new Effect("Ventum Maris", 174),
    	new Effect("Mirthful Mistletoe", 175),
    	new Effect("Delightful Star", 3049),
    	new Effect("Frosted Star", 3050),
    	new Effect("Apotheosis", 3051),
    	new Effect("Ascension", 3052),
    	new Effect("Reindoonicorn Rancher", 3053),
    	new Effect("Reindoonicorn Rancher", 3054),
    	new Effect("Twinkling Lights", 3055),
    	new Effect("Shimmering Lights", 3056)
	);

	private static final Map<Integer, Effect> intEffectLookup = new HashMap<>();
	private static final Map<String, Effect> stringEffectLookup = new HashMap<>();
	static{
		for(Effect e : allEffects){
			intEffectLookup.put(e.code, e);
			stringEffectLookup.put(e.name.toLowerCase(), e);
		}
	}

	private Effect(String name, int code){
		this.name = name;
		this.code = code;
	}

	/**Returns this effect's name.
	@return this effect's name.
	*/
	public String getName(){
		return this.name;
	}

	/**Returns this effect's int value.
	@return this effect's int value.
	*/
	public int getIntValue(){
		return this.code;
	}

	/**Returns an Effect corresponding to the given effect name, case insensitive.
	@throws NullPointerException if effectName is null.
	@throws NoSuchElementException if no unusual effect corresponds to the given String.
	@return an Effect corresponding to the given effect name.
	*/
	public static Effect forName(String effectName){
		if(!stringEffectLookup.containsKey(effectName.toLowerCase())){
			throw new NoSuchElementException("No effect with name " + effectName + " exists.");
		}
		return stringEffectLookup.get(effectName.toLowerCase());
	}

	/**Returns an Effect corresponding to the given effect int value.
	@throws NoSuchElementException if no unusual effect corresponds to the given int.
	@return an Effect corresponding to the given effect int value.
	*/
	public static Effect forInt(int effectIndex){
		if(!intEffectLookup.containsKey(effectIndex)){
			throw new NoSuchElementException("No effect with code " + effectIndex + " exists.");
		}
		return intEffectLookup.get(effectIndex);
	}

	/**Returns a String representation of this Effect.
	@return a String representation of this Effect.
	*/
	@Override
	public String toString(){
		return "trading.economy.Effect: " + this.name;
	}
}