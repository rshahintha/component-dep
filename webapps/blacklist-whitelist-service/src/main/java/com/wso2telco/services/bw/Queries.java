/**
 * Copyright (c) 2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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

package com.wso2telco.services.bw;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wso2telco.core.dbutils.exception.BusinessException;
import com.wso2telco.core.msisdnvalidator.InvalidMSISDNException;
import com.wso2telco.dep.bw.model.RequestError;
import com.wso2telco.dep.operatorservice.model.BlackListDTO;
import com.wso2telco.dep.operatorservice.model.MSISDNSearchDTO;
import com.wso2telco.dep.operatorservice.model.ProvisionReq;
import com.wso2telco.dep.operatorservice.model.WhiteListDTO;
import com.wso2telco.dep.operatorservice.service.BlackListWhiteListService;
import com.wso2telco.dep.operatorservice.service.OparatorService;
import com.wso2telco.services.bw.entity.BlackList;
import com.wso2telco.services.bw.entity.BlackListBulk;
import com.wso2telco.services.bw.entity.RemoveRequest;
import com.wso2telco.services.bw.entity.WhiteList;
import com.wso2telco.services.bw.entity.WhiteListBulk;

@Path("/queries")
public class Queries {

	private static final Logger LOG = Logger.getLogger(Queries.class.getName());
	@Context
	private UriInfo context;

	BlackListWhiteListService blackListWhiteListService;
	OparatorService oparatorService = null;

	{
		oparatorService = new OparatorService();
		blackListWhiteListService = new BlackListWhiteListService();
	}

	/**
	 * GET method for creating an instance of QueriesResource
	 *
	 * @param address,requestedAccuracy
	 *            representation for the new resource
	 * @return an HTTP response with content of the created resource
	 * @throws Exception
	 */
	@POST
	@Path("/Blacklist/{MSISDN}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response location(@PathParam("MSISDN") String msisdn, String jsonBody) throws Exception {
		LOG.debug("location Triggerd  jsonBody :" + jsonBody + " jsonBody: " + jsonBody);
		Gson gson = new GsonBuilder().serializeNulls().create();

		final StringBuilder errorMSG = new StringBuilder();
		final StringBuilder jsonreturn = new StringBuilder();

		errorMSG.append("{").append("\"Failed\":".intern()).append("{".intern());
		errorMSG.append("\"messageId\":\"".intern()).append("Blacklist Number".intern()).append("\",".intern());
		errorMSG.append("\"text\":\"".intern()).append("Blacklist Number could not be added to the system ".intern())
				.append("\",");
		errorMSG.append("\"variables\":\"".intern()).append(msisdn).append("\"");
		errorMSG.append("}}");

		BlackList blackListReq = gson.fromJson(jsonBody, BlackList.class);

		String apiID = blackListReq.getAPIID();
		String apiName = blackListReq.getAPIName();
		String userID = blackListReq.getUserID();

		if (apiID != null && apiName != null && userID != null) {

			try {
				BlackListDTO blackListDTO = new BlackListDTO();
				blackListDTO.setApiID(apiID);
				blackListDTO.setApiName(apiName);
				blackListDTO.setUserID(userID);
				blackListDTO.setUserMSISDN(msisdn);

				blackListWhiteListService.blacklist(blackListDTO);

				jsonreturn.append("{").append("\"Success\":".intern()).append("{" + "\"messageId\":\"".intern())
						.append("Blacklist Number".intern()).append("\",");
				jsonreturn.append("\"text\":\"").append("Blacklist Number Successfully Added to the system ".intern())
						.append("\",");
				jsonreturn.append("\"variables\":\"").append(msisdn).append("\"").append("}}");

				return Response.status(Response.Status.OK).entity(jsonreturn.toString()).build();
			} catch (BusinessException msisdnEx) {
				return Response.status(Response.Status.BAD_REQUEST).entity(msisdnEx.getErrorType()).build();
			}
		} else {
			return Response.status(Response.Status.BAD_REQUEST).entity(errorMSG.toString()).build();
		}

	}

	@POST
	@Path("/Blacklist")
	@Consumes("application/json")
	@Produces("text/plain")
	public Response location(String jsonBody) {

		LOG.debug("location Triggerd  jsonBody :" + jsonBody);

		Gson gson = new GsonBuilder().serializeNulls().create();

		BlackListBulk blackListReq = gson.fromJson(jsonBody, BlackListBulk.class);

		String apiID = blackListReq.getAPIID();
		String apiName = blackListReq.getAPIName();
		String userID = blackListReq.getUserID();
		String[] msisdnList = blackListReq.getMsisdnList();

		final StringBuilder errorMSG = new StringBuilder();

		errorMSG.append("{").append("\"Failed\":").append("{").append("\"messageId\":\"".intern())
				.append("Blacklist Numbers".intern()).append("\",");
		errorMSG.append("\"text\":\"").append("Blacklist Numbers could not be added to the system ".intern())
				.append("\",");
		errorMSG.append("\"variables\":").append(gson.toJson(msisdnList)).append("}}");

		final StringBuilder successMSG = new StringBuilder();
		successMSG.append("{").append("\"Success\":").append("{").append("\"messageId\":\"").append("Blacklist Numbers")
				.append("\",");
		successMSG.append("\"text\":\"").append("Blacklist Numbers Successfully Added to the system ").append("\",");
		successMSG.append("\"variables\":").append(gson.toJson(msisdnList)).append("}}");

		gson = new Gson();

		if (apiID != null && apiName != null && userID != null && msisdnList != null) {
			try {
				BlackListDTO blackListDTO = new BlackListDTO();
				blackListDTO.setApiID(apiID);
				blackListDTO.setApiName(apiName);
				blackListDTO.setUserID(userID);
				blackListDTO.setUserMSISDN(msisdnList);

				blackListWhiteListService.blacklist(blackListDTO);

				return Response.status(Response.Status.OK).entity(successMSG.toString()).build();

			} catch (BusinessException msisdnEx) {

				return Response.status(Response.Status.BAD_REQUEST).entity(msisdnEx.getErrorType()).build();

			}

		} else {
			return Response.status(Response.Status.BAD_REQUEST).entity(errorMSG.toString()).build();
		}

	}

	private String[] removeNullMsisdnValues(String[] msisdnList) {
		List<String> list = new ArrayList<String>(Arrays.asList(msisdnList));
		list.removeAll(Collections.singleton(null));
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Returns the blacklist per API
	 *
	 * {empty request body}
	 */
	@POST
	@Path("/GetBlacklistPerApi/{APINAME}")
	@Consumes("application/json")
	@Produces("text/plain")
	public Response getBlacklistPerApi(@PathParam("APINAME") String apiName) {
		LOG.debug("getBlacklistPerApi Triggerd  apiName :" + apiName);

		Gson gson = new Gson();
		StringBuilder errorMSG = new StringBuilder();
		errorMSG.append("{" + "\"Failed\":").append("{").append("\"messageId\":\"").append("Blacklist result")
				.append("\",").append("\"text\":\"");
		errorMSG.append("Blacklist numbers could not be retrieved").append("\"" + "}}");

		if (apiName != null) {

			try {
				MSISDNSearchDTO searchDTO = new MSISDNSearchDTO();
				searchDTO.setApiName(apiName);

				String[] blacklist = blackListWhiteListService.loadBlacklisted(searchDTO);
				StringBuilder successMSG = new StringBuilder();

				successMSG.append("{").append("\"Success\":").append("{").append("\"messageId\":\"")
						.append("Blacklist result").append("\",");
				successMSG.append("\"text\":\"").append("Blacklist numbers were retrieved from the system".intern())
						.append("\",").append("\"variables\":");
				successMSG.append(gson.toJson(blacklist)).append("}}");

				return Response.status(Response.Status.OK).entity(successMSG.toString()).build();
			} catch (BusinessException e) {
				return Response.status(Response.Status.BAD_REQUEST).entity(e.getErrorType()).build();
			}
		} else {
			return Response.status(Response.Status.BAD_REQUEST).entity(errorMSG.toString()).build();
		}
	}

	/**
	 * Removes from the blacklist
	 *
	 * { "apiName":"USSD" }
	 */
	@POST
	@Path("/RemoveFromBlacklist/{MSISDN}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response removeFromBlacklist(@PathParam("MSISDN") String msisdn, String jsonBody) throws SQLException {
		LOG.debug("removeFromBlacklist Triggerd  jsonBody :" + jsonBody + " , msisdn :" + msisdn);

		Gson gson = new GsonBuilder().serializeNulls().create();

		RemoveRequest removeReq = gson.fromJson(jsonBody, RemoveRequest.class);

		StringBuilder erroMsg = new StringBuilder();
		erroMsg.append("{").append("\"Failed\":").append("{").append("\"messageId\":\"").append("Remove Number")
				.append("\",").append("\"text\":\"");
		erroMsg.append("Blacklist number could not be removed ".intern()).append("\",").append("\"variables\":\"")
				.append(msisdn).append("\"").append("}}");

		String apiName = removeReq.getAPIName();

		if (apiName != null) {

			try {
				blackListWhiteListService.removeBlacklist(apiName, msisdn);

				StringBuilder jsonreturn = new StringBuilder();
				jsonreturn.append("{").append("\"Success\":").append("{").append("\"messageId\":\"")
						.append("Remove Number").append("\",").append("\"text\":\"");
				jsonreturn.append("Blacklist number successfully removed ").append("\",").append("\"variables\":\"")
						.append(msisdn).append("\"").append("}}");

				return Response.status(Response.Status.OK).entity(jsonreturn).build();
			} catch (BusinessException e) {
				LOG.error("", e);

				return Response.status(Response.Status.BAD_REQUEST).entity(e.getErrorType()).build();
			}
		} else {
			return Response.status(Response.Status.BAD_REQUEST).entity(erroMsg.toString()).build();
		}

	}

	/**
	 * GET method for creating an instance of QueriesResource
	 *
	 * @param address,requestedAccuracy
	 *            representation for the new resource
	 * @return an HTTP response with content of the created resource
	 */
	@POST
	@Path("/whitelist/{MSISDN}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response provisionWhiteListedNumber(@PathParam("MSISDN") String msisdn, String jsonBody) {

		LOG.debug("provisionWhiteListedNumber Triggerd  jsonBody :" + jsonBody + " , msisdn :" + msisdn);

		Gson gson = new GsonBuilder().serializeNulls().create();

		WhiteList whiteListReq = gson.fromJson(jsonBody, WhiteList.class);

		String subscriptionID = whiteListReq.getSubscriptionID();
		String apiID = whiteListReq.getAPIID();
		String applicationID = whiteListReq.getApplicationID();
		final StringBuilder errorMSG = new StringBuilder();

		errorMSG.append("{").append("\"Failed\":".intern()).append("{").append("\"messageId\":\"".intern())
				.append("Whitelisted Number".intern()).append("\",");
		errorMSG.append("\"text\":\"").append("Whitelisted Number could not be added to the system ".intern())
				.append("\",");
		errorMSG.append("\"variables\":\"").append(msisdn).append("\"").append("}}");

		if (subscriptionID != null && apiID != null && applicationID != null) {

			try {

				WhiteListDTO whiteListDTO = new WhiteListDTO();
				whiteListDTO.setApiID(apiID);
				whiteListDTO.setApplicationID(applicationID);
				whiteListDTO.setSubscriptionID(subscriptionID);
				whiteListDTO.setUserMSISDN(msisdn);

				blackListWhiteListService.whiteListSubscription(whiteListDTO);

				final StringBuilder succMSG = new StringBuilder();

				succMSG.append("{").append("\"Success\":").append("{").append("\"messageId\":\"")
						.append("WhiteListed Number".intern()).append("\",");
				succMSG.append("\"text\":\"").append("WhiteListed Number Successfully Added to the system ".intern())
						.append("\",");
				succMSG.append("\"variables\":\"").append(msisdn).append("\"").append("}}");

				return Response.status(Response.Status.OK).entity(succMSG.toString()).build();

			}catch( InvalidMSISDNException e1){
				return Response.status(Response.Status.BAD_REQUEST).entity(e1.getErrorType()).build();
				
			} catch (BusinessException e) {
				return Response.status(Response.Status.BAD_REQUEST).entity(e.getErrorType()).build();
			}

		}

		else {
			return Response.status(Response.Status.BAD_REQUEST).entity(errorMSG.toString()).build();
		}

	}

	@POST
	@Path("/Whitelist")
	@Consumes("application/json")
	@Produces("application/json")
	public Response bulkWhitelist(String jsonBody) throws SQLException {
		LOG.debug("bulkWhitelist Triggerd  jsonBody :" + jsonBody);

		String jsonreturn = null;
		Gson gson = new GsonBuilder().serializeNulls().create();
		WhiteListBulk whiteListReq = gson.fromJson(jsonBody, WhiteListBulk.class);

		String appId = whiteListReq.getAppId();
		String apiId = whiteListReq.getApiId();
		// String apiName = whiteListReq.getAPIName();
		String userID = whiteListReq.getUserID();
		String[] msisdnList = whiteListReq.getMsisdnList();

		StringBuilder errorMSG = new StringBuilder();
		errorMSG.append("{\"Failed\":{\"messageId\":\"Whitelist Numbers\",".intern());
		errorMSG.append("\"text\":\"Whitelist Numbers could not be added to the system \",".intern());
		errorMSG.append("\"variables\":").append(gson.toJson(msisdnList)).append("}}");

		gson = new Gson();

		// if (apiID != null && apiName != null && userID != null && msisdnList
		// != null) {
		if (userID != null && msisdnList != null) {
			msisdnList = removeNullMsisdnValues(msisdnList);

			try {

				WhiteListDTO whiteListDTO = new WhiteListDTO();
				whiteListDTO.setApiID(apiId);
				whiteListDTO.setApplicationID(appId);
				whiteListDTO.setUserMSISDN(msisdnList);

				blackListWhiteListService.whiteListSubscription(whiteListDTO);

				StringBuilder succMSG = new StringBuilder();

				succMSG.append("{\"Success\":{\"messageId\":\"Whitelist Numbers\",".intern());
				succMSG.append("\"text\":\" Whitelist Numbers Successfully Added to the system \",".intern());
				succMSG.append("\"variables\":").append(gson.toJson(msisdnList)).append("}}");

				return Response.status(Response.Status.OK).entity(succMSG.toString()).build();
			}catch( InvalidMSISDNException e1){
				return Response.status(Response.Status.BAD_REQUEST).entity(e1.getErrorType()).build();
			} 
			catch (BusinessException ex) {
				return Response.status(Response.Status.BAD_REQUEST).entity(ex.getErrorType()).build();
			}
		} else {
			return Response.status(Response.Status.BAD_REQUEST).entity(errorMSG.toString()).build();
		}

	}

	/*	*//**
			 * get all subscribers
			 *//*
			 * @POST
			 * 
			 * @Path("/getSubscribers")
			 * 
			 * @Consumes("application/json")
			 * 
			 * @Produces("application/json") public Response
			 * getSubscribers(String jsonBody) { try { LOG.debug(
			 * "getSubscribers Triggerd  jsonBody :" + jsonBody);
			 * 
			 * String subscribersJson = DatabaseUtils.getSubscribers(); return
			 * Response.status(Response.Status.OK).entity(subscribersJson).build
			 * (); } catch (Exception ex) {
			 * java.util.logging.Logger.getLogger(Queries.class.getName()).log(
			 * Level.SEVERE, null, ex); return
			 * Response.status(Response.Status.BAD_REQUEST).entity(
			 * "{\"error\":\"true\"}").build(); } }
			 */
	/**
	 * get all apps of subscriber
	 */
	/*
	 * @POST
	 * 
	 * @Path("/getApps")
	 * 
	 * @Consumes("application/json")
	 * 
	 * @Produces("application/json") public Response getApps(String jsonBody) {
	 * try { LOG.debug("getApps Triggerd  msisdn :" + jsonBody);
	 * 
	 * Gson gson = new GsonBuilder().serializeNulls().create(); Id whiteListReq
	 * = gson.fromJson(jsonBody, Id.class);
	 * 
	 * String subscriberId = whiteListReq.getId(); String json =
	 * DatabaseUtils.getApps(subscriberId); return
	 * Response.status(Response.Status.OK).entity(json).build(); } catch
	 * (Exception ex) {
	 * java.util.logging.Logger.getLogger(Queries.class.getName()).log(Level.
	 * SEVERE, null, ex); return
	 * Response.status(Response.Status.BAD_REQUEST).entity(
	 * "{\"error\":\"true\"}").build(); } }
	 */

	/**
	 * get apis of app
	 *//*
		 * @POST
		 * 
		 * @Path("/getApis")
		 * 
		 * @Consumes("application/json")
		 * 
		 * @Produces("application/json") public Response getApis(String
		 * jsonBody) { try { Gson gson = new
		 * GsonBuilder().serializeNulls().create(); Id whiteListReq =
		 * gson.fromJson(jsonBody, Id.class);
		 * 
		 * String appId = whiteListReq.getId(); String json =
		 * DatabaseUtils.getApis(appId); return
		 * Response.status(Response.Status.OK).entity(json).build(); } catch
		 * (Exception ex) {
		 * java.util.logging.Logger.getLogger(Queries.class.getName()).log(Level
		 * .SEVERE, null, ex); return
		 * Response.status(Response.Status.BAD_REQUEST).entity(
		 * "{\"error\":\"true\"}").build(); } }
		 */

	@POST
	@Path("/GetWhiteList")
	@Consumes("application/json")
	@Produces("text/plain")
	public Response getWhiteListNumbers() throws SQLException {

		Gson gson = new Gson();

		try {
			String[] whiteListNumbers = blackListWhiteListService.getWhiteListNumbers();

			StringBuilder successMSG = new StringBuilder();
			successMSG.append("{\"Success\":{\"messageId\":\"Whitelist result\",\"text\":\"");
			successMSG.append("WhiteList numbers were retrieved from the system\",\"variables\":");
			successMSG.append(gson.toJson(whiteListNumbers)).append("}}");

			return Response.status(Response.Status.OK).entity(successMSG.toString()).build();

		} catch (BusinessException e) {

			StringBuilder errorMSG = new StringBuilder();

			errorMSG.append("{\"Failed\":{\"messageId\":\"Whitelist result\",\"text\":\"");
			errorMSG.append("WhiteList numbers could not be retrieved" + "\"" + "}}");

			return Response.status(Response.Status.BAD_REQUEST).entity(e.getErrorType()).build();
		}

	}

	@POST
	@Path("/RemoveFromWhiteList/{MSISDN}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response removeFromWhiteListNumbers(@PathParam("MSISDN") String msisdn) {

		LOG.debug("removeFromWhiteListNumbers Triggerd  msisdn :" + msisdn);

		try {
			blackListWhiteListService.removeWhitelistNumber(msisdn);

			StringBuilder succMSG = new StringBuilder();

			succMSG.append("{\"Success\":{\"messageId\":\"Remove Number\",\"text\":\"");
			succMSG.append("Whitelist number successfully removed \",\"variables\":\"").append(msisdn)
					.append("\"" + "}}");

			return Response.status(Response.Status.OK).entity(succMSG.toString()).build();

		} catch (BusinessException e) {
			LOG.error("removeFromWhiteListNumbers", e);

			StringBuilder errorMSG = new StringBuilder();

			errorMSG.append("{\"Failed\":{\"messageId\":\"Remove Number\",\"text\":\"".intern());
			errorMSG.append("Blacklist number could not be removed \",\"variables\":\"").append(msisdn)
					.append("\"" + "}}");

			return Response.status(Response.Status.BAD_REQUEST).entity(e.getErrorType()).build();

		}

	}

	/**
	 * Merchantinsert.
	 *
	 * @param jsonData
	 *            the json data
	 * @return the response
	 */
	@POST
	@Path("/merchant/blacklist")
	@Consumes("application/json")
	@Produces("application/json")
	public Response blacklistAggregator(String jsonData) {

		try {

			LOG.debug("jsondata: " + jsonData);

			ProvisionReq provisionreq = new Gson().fromJson(jsonData, ProvisionReq.class);

			oparatorService.blacklistAggregator(provisionreq);

		} catch (BusinessException e) {
			jsonData = new Gson().toJson(new RequestError(e.getErrorType()));
			LOG.error("", e);
			return Response.status(Response.Status.BAD_REQUEST).entity(jsonData).build();
		}
		LOG.debug("Aggregators blacklist  success jsonData :" + jsonData);
		return Response.status(Response.Status.CREATED).build();
	}
}
