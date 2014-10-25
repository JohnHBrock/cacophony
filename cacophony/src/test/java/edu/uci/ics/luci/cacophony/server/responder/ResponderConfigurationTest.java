package edu.uci.ics.luci.cacophony.server.responder;

import static org.junit.Assert.fail;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import net.minidev.json.JSONValue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.cacophony.server.CNodeServer;
import edu.uci.ics.luci.cacophony.server.CNodeServerTest;
import edu.uci.ics.luci.cacophony.server.ConfigurationsDAO;
import edu.uci.ics.luci.cacophony.server.P2PSinkTest;
import edu.uci.ics.luci.p2pinterface.P2PInterface;

public class ResponderConfigurationTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		ConfigurationsDAO.enableTestingMode();
	}

	@After
	public void tearDown() throws Exception {
		ConfigurationsDAO.disableTestingMode();
	}
	
	@Test
	public void testDegenerate() {

		try{
			new ResponderConfigurationLoader(null);
			fail("Should throw an exception");
		}
		catch(IllegalArgumentException e){
			//Expected
		}
	}
	
	@Test
	public void testResponder() {
		
		try{
			/* Set up the server that we are testing */
			CNodeServer cNodeServer = new CNodeServer(CNodeServerTest.makeARandomP2PServerAddress());
			cNodeServer.start();
			
			/* Make an interface to send messages to the server and load 3 configurations */
			String testName1 = cNodeServer.getServerName()+"01";
			P2PSinkTest p2pSinkTest = new P2PSinkTest(cNodeServer);
			p2pSinkTest.addPassPhrase("\\Q{\"responses\":[{\"status\":\"OK\",\"clone_ID\":\"\\E[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\Q\",\"source_ID\":\"c_node_01\"},{\"status\":\"OK\",\"clone_ID\":\"\\E[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\Q\",\"source_ID\":\"c_node_02\"},{\"status\":\"OK\",\"clone_ID\":\"\\E[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\Q\",\"source_ID\":\"c_node_03\"}]}\\E");
			P2PInterface p2p = new P2PInterface(testName1, p2pSinkTest);
			p2p.start();
			JSONObject request = ResponderConfigurationLoaderTest.makeLoadConfigurationRequest(testName1,testName1);
			p2p.sendMessage(cNodeServer.getServerName(), request.toJSONString(JSONStyle.LT_COMPRESS));
			
			/* Wait for a response */
			String responsesString = P2PSinkTest.waitForResponse(p2pSinkTest);
			JSONObject responsesJSON = (JSONObject)JSONValue.parse(responsesString);
			JSONArray responses = (JSONArray)responsesJSON.get("responses");
			JSONObject response3 = (JSONObject)responses.get(2);
			String ID3 = response3.get("clone_ID").toString();
			
			p2pSinkTest.addPassPhrase("\\Q{\"responses\":[{\"c_nodes\":[\"\\E[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\Q\",\"\\E[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\Q\",\"\\E[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\Q\"],\"server_capacity\":\"\\E\\d+\\Q\",\"server_capabilities\":{\"load_configurations\":\"edu.uci.ics.luci.cacophony.server.responder.ResponderConfigurationLoader\",\"capabilities\":\"edu.uci.ics.luci.cacophony.server.responder.ResponderCapabilities\",\"configuration\":\"edu.uci.ics.luci.cacophony.server.responder.ResponderConfiguration\",\"null\":\"edu.uci.ics.luci.cacophony.server.responder.ResponderCapabilities\",\"shutdown\":\"edu.uci.ics.luci.cacophony.server.responder.ResponderShutdown\"}}]}\\E");
			request.put("request","capabilities");
			request.remove("data");
			p2p.sendMessage(cNodeServer.getServerName(), request.toJSONString(JSONStyle.LT_COMPRESS));
			
			/* Wait for a response */
			P2PSinkTest.waitForResponse(p2pSinkTest);
			
			
			/* Test a bad request */
			p2pSinkTest.addPassPhrase("\\Q{\"errors\":[\"No \\\"data\\\" sent in the incoming JSON into a String\"]}\\E");
			request.put("request","configuration");
			request.remove("data");
			p2p.sendMessage(cNodeServer.getServerName(), request.toJSONString(JSONStyle.LT_COMPRESS));
			
			/* Wait for a response */
			P2PSinkTest.waitForResponse(p2pSinkTest);
			
			
			/* Send the request we are testing */
			String passPhrase = "\\Q{\"responses\":[{\"predictors\":[\"p2p://"+cNodeServer.getServerName()+"01/c_node_02\"],\"c_node_name\":\"c_node_03\",\"features\":[{\"translator\":{\"classname\":\"edu.uci.ics.luci.cacophony.node.TranslatorString\",\"options\":{\"a\":\"thing\"}},\"path_expression\":\"//*[@id=\\\"cnn_ftrcntntinner\\\"]/div[9]/div[1]/text()[2]\",\"name\":\"feature 1\",\"ID\":\"ID of feature 1\",\"reg_ex\":\"(.*)\",\"format\":\"html\",\"url\":\"http://www.cnn.com\"}],\"target\":{\"translator\":{\"classname\":\"edu.uci.ics.luci.cacophony.node.TranslatorString\",\"options\":{\"c\":\"yet another thing\"}},\"path_expression\":\"//*[@id=\\\\\\\"cnn_ftrcntntinner\\\\\\\"]/div[9]/div[1]/text()[2]\",\"name\":\"name of target\",\"ID\":\"ID of target\",\"reg_ex\":\"temp=(.*)\",\"format\":\"html\",\"url\":\"http://cnn.com\"},\"polling\":{\"min_interval\":\"5000\",\"policy\":\"ON_CHANGE\"}}]}\\E";
			p2pSinkTest.addPassPhrase(passPhrase);
			request.put("request","configuration");
			JSONObject data = new JSONObject();
			data.put("c_node", ID3);
			request.put("data",data);
			p2p.sendMessage(cNodeServer.getServerName(), request.toJSONString(JSONStyle.LT_COMPRESS));
			
			/* Wait for a response */
			P2PSinkTest.waitForResponse(p2pSinkTest);

			
			/* Shut down the server and wait for it */
			synchronized(cNodeServer.getQuittingMonitor()){
				while(!cNodeServer.isQuitting()){
					cNodeServer.stop();
					try {
						if(!cNodeServer.isQuitting()){
							cNodeServer.getQuittingMonitor().wait();
						}
					} catch (InterruptedException e) {
					}
				}
			}
		}
		catch(RuntimeException e){
			System.err.println(e.getMessage());
			fail("This should not fail."+e);
		}
	}

}
