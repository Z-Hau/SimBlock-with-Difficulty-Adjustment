/**
 * Copyright 2019 Distributed Systems Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package SimBlock.settings;

public class SimulationConfiguration {
	public static final int NUM_OF_NODES = 600;//600(doge);//800(lite);//6000(bitcoin);

	public static final String TABLE = "SimBlock.node.routingTable.BitcoinCoreTable";

	// Unit: millisecond
	public static final long INTERVAL = 1000*60*10;//1000*60(doge);//1000*30*5(lite);//1000*60*10(bitcoin);

	// Mining power is the number of mining (hash calculation) executed per millisecond.
	public static  int AVERAGE_MINING_POWER = 400000; //400000
	public static final int STDEV_OF_MINING_POWER = 100000;

	public static final int ENDBLOCKHEIGHT = 100; //579972 2019-06-01 12:04 AM - 610638 2020-01-01 00:08 (30666)

	// Unit: byte
	public static final long BLOCKSIZE = 535000;//6110(lite);//8000(doge);//535000(bitcoin);//0.5MB

	public static final int DIFFICULTY_INTERVAL = 0; //readjust difficulty once every N block
	public static final int CHANGE_MINING_POWER_INTERVAL = 0; //1(every block); 0(no changes)
	public static final double MINING_POWER_INCREASE_PERCENTAGE = 0.5; //the chance to increase
	public static final double MINING_POWER_CHANGE_RATIO = 0.1; //how much to increase or decrease
	public static final String SIMULATION_TYPE = "bitcoin"; //dogecoin; litecoin; bitcoin
}