native("jvm") // TODO: remove this
module strategicprimer.mining "0.4.9017" {
	value ceylonVersion = "1.3.3";
	value javaVersion = "8";
	value spVersion = "0.4.9017";
	value lovelaceUtilsVersion = "0.1.0";
	import lovelace.util.common lovelaceUtilsVersion;
	native("jvm")
	shared import ceylon.file ceylonVersion;
	native("jvm")
	import java.base javaVersion;
	native("jvm")
	shared import strategicprimer.drivers.common spVersion;
	import ceylon.collection ceylonVersion;
}
