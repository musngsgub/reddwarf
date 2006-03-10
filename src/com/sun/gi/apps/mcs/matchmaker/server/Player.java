/*
 * Copyright © 2006 Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, California 95054, U.S.A. All rights reserved.
 * 
 * Sun Microsystems, Inc. has intellectual property rights relating to
 * technology embodied in the product that is described in this
 * document. In particular, and without limitation, these intellectual
 * property rights may include one or more of the U.S. patents listed at
 * http://www.sun.com/patents and one or more additional patents or
 * pending patent applications in the U.S. and in other countries.
 * 
 * U.S. Government Rights - Commercial software. Government users are
 * subject to the Sun Microsystems, Inc. standard license agreement and
 * applicable provisions of the FAR and its supplements.
 * 
 * Use is subject to license terms.
 * 
 * This distribution may include materials developed by third parties.
 * 
 * Sun, Sun Microsystems, the Sun logo and Java are trademarks or
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other
 * countries.
 * 
 * This product is covered and controlled by U.S. Export Control laws
 * and may be subject to the export or import laws in other countries.
 * Nuclear, missile, chemical biological weapons or nuclear maritime end
 * uses or end users, whether direct or indirect, are strictly
 * prohibited. Export or reexport to countries subject to U.S. embargo
 * or to entities identified on U.S. export exclusion lists, including,
 * but not limited to, the denied persons and specially designated
 * nationals lists is strictly prohibited.
 * 
 * Copyright © 2006 Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, California 95054, Etats-Unis. Tous droits réservés.
 * 
 * Sun Microsystems, Inc. détient les droits de propriété intellectuels
 * relatifs à la technologie incorporée dans le produit qui est décrit
 * dans ce document. En particulier, et ce sans limitation, ces droits
 * de propriété intellectuelle peuvent inclure un ou plus des brevets
 * américains listés à l'adresse http://www.sun.com/patents et un ou les
 * brevets supplémentaires ou les applications de brevet en attente aux
 * Etats - Unis et dans les autres pays.
 * 
 * L'utilisation est soumise aux termes de la Licence.
 * 
 * Cette distribution peut comprendre des composants développés par des
 * tierces parties.
 * 
 * Sun, Sun Microsystems, le logo Sun et Java sont des marques de
 * fabrique ou des marques déposées de Sun Microsystems, Inc. aux
 * Etats-Unis et dans d'autres pays.
 * 
 * Ce produit est soumis à la législation américaine en matière de
 * contrôle des exportations et peut être soumis à la règlementation en
 * vigueur dans d'autres pays dans le domaine des exportations et
 * importations. Les utilisations, ou utilisateurs finaux, pour des
 * armes nucléaires,des missiles, des armes biologiques et chimiques ou
 * du nucléaire maritime, directement ou indirectement, sont strictement
 * interdites. Les exportations ou réexportations vers les pays sous
 * embargo américain, ou vers des entités figurant sur les listes
 * d'exclusion d'exportation américaines, y compris, mais de manière non
 * exhaustive, la liste de personnes qui font objet d'un ordre de ne pas
 * participer, d'une façon directe ou indirecte, aux exportations des
 * produits ou des services qui sont régis par la législation américaine
 * en matière de contrôle des exportations et la liste de ressortissants
 * spécifiquement désignés, sont rigoureusement interdites.
 */

package com.sun.gi.apps.mcs.matchmaker.server;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.sun.gi.apps.mcs.matchmaker.server.CommandProtocol.*;

import com.sun.gi.comm.routing.ChannelID;
import com.sun.gi.comm.routing.UserID;
import com.sun.gi.logic.GLOReference;
import com.sun.gi.logic.SimTask;
import com.sun.gi.logic.SimUserDataListener;
import com.sun.gi.utils.SGSUUID;

/**
 * <p>
 * Title: Player
 * </p>
 * 
 * <p>
 * Description: This class represents a Player that is currently
 * on-line. The Player object acts as a "command proxy" for the
 * associated user. Commands arrive via userDataReceived callback on the
 * Lobby Manager control channel. The command is processed and a
 * response sent back to the user on the same channel.
 * </p>
 * 
 * @author Sten Anderson
 * @version 1.0
 */
public class Player implements SimUserDataListener {

    private static final long serialVersionUID = 1L;

    private String userName;
    private UserID userID;
    private GLOReference<Folder>   folderRoot;
    private GLOReference<Lobby>    currentLobby;
    private GLOReference<GameRoom> currentGameRoom;

    private CommandProtocol protocol;

    public Player(UserID uid, String userName, GLOReference<Folder> root) {
        this.userID = uid;
        this.userName = userName;
        this.folderRoot = root;

        protocol = new CommandProtocol();
    }

    public GLOReference<Lobby> getCurrentLobby() {
        return currentLobby;
    }

    /**
     * All command protocols for a connected user come in on this call
     * back. The command is parsed/processed and a response is sent out
     * on the LobbyManager control channel.
     * 
     * @see com.sun.gi.logic.SimUserDataListener#userDataReceived
     */
    public void userDataReceived(UserID from, ByteBuffer data) {
        SimTask task = SimTask.getCurrent();
        System.out.println("UserDataReceived from: " + from.toString());
        int commandCode = protocol.readUnsignedByte(data);
        if (commandCode == LIST_FOLDER_REQUEST) {
            listFolderRequest(task, data);
        } else if (commandCode == LOOKUP_USER_ID_REQUEST) {
            lookupUserIDRequest(task, data);
        } else if (commandCode == LOOKUP_USER_NAME_REQUEST) {
            lookupUserNameRequest(task, data);
        } else if (commandCode == FIND_USER_REQUEST) {
            locateUserRequest(task, data);
        } else if (commandCode == JOIN_LOBBY) {
            joinLobby(task, data);
        } else if (commandCode == JOIN_GAME) {
            joinGame(task, data);
        } else if (commandCode == GAME_PARAMETERS_REQUEST) {
            gameParametersRequest(task);
        } else if (commandCode == CREATE_GAME) {
            createGame(task, data);
        } else if (commandCode == UPDATE_PLAYER_READY_REQUEST) {
            updatePlayerReadyRequest(task, data);
        } else if (commandCode == START_GAME_REQUEST) {
            startGame(task);
        } else if (commandCode == LEAVE_LOBBY) {
        	leaveLobby(task);
        } else if (commandCode == LEAVE_GAME) {
        	leaveGame(task);
        }
        

    }

    public void dataArrivedFromChannel(ChannelID id, UserID from,
            ByteBuffer buff)
    {

    }

    /**
     * <p>
     * This callback is called when a user joins a channel. The joined
     * channel could be the Lobby Manager control channel, a lobby, or a
     * game room.
     * </p>
     * 
     * <p>
     * If the channel joined is the lobby manager control channel, then
     * the SERVER_LISTENING response is sent down the channel to the
     * user.
     * </p>
     * 
     * @see com.sun.gi.logic.SimUserDataListener#userJoinedChannel
     */
    public void userJoinedChannel(ChannelID cid, UserID uid) {
        SimTask task = SimTask.getCurrent();
        if (uid.equals(userID)) { // it was this player who joined,
                                    // add to the either the lobby or
                                    // game list.
            if (cid.equals(task.openChannel(LOBBY_MANAGER_CONTROL_CHANNEL))) {
                List list = protocol.createCommandList(SERVER_LISTENING);
                sendResponse(task, list);
                
                return;
            }
            
            GLOMap<SGSUUID, GLOReference<Lobby>> lobbyMap =
                (GLOMap<SGSUUID, GLOReference<Lobby>>) task.findGLO("LobbyMap").peek(task);
            GLOReference<Lobby> lobbyRef = lobbyMap.get(cid);
            if (lobbyRef != null) {
                currentLobby = lobbyRef;
                Lobby lobby = lobbyRef.get(task);
                lobby.addUser(uid);
                
                // send the games to the user
                for (GLOReference<GameRoom> gRef : lobby.getGameRoomList()) {
                	GameRoom curGame = gRef.peek(task);
                    List list = protocol.createCommandList(GAME_CREATED);
                    packGameDescriptor(list, curGame);
                    sendResponse(task, list, cid);
                }

                // send lobby joined message
                List list = protocol.createCommandList(PLAYER_ENTERED_LOBBY);
                list.add(userID);
                list.add(userName);
                sendMulticastResponse(task, lobby.getUsers(), list, cid);
                
                sendRetroJoins(task, lobby.getUsers(), PLAYER_ENTERED_LOBBY, cid);
                
            } else { // must have been a game room
                GLOMap<SGSUUID, GLOReference<GameRoom>> gameRoomMap =
                    (GLOMap<SGSUUID, GLOReference<GameRoom>>) task.findGLO(
                        "GameRoomMap").peek(task);
                GLOReference<GameRoom> gameRef = gameRoomMap.get(cid);
                if (gameRef != null) {
                    currentGameRoom = gameRef;
                    GameRoom gameRoom = gameRef.get(task);
                    gameRoom.addUser(uid);
                    
                    // send playerJoinedGame message to lobby
                    List lobbyList = protocol.createCommandList(PLAYER_JOINED_GAME);
                    lobbyList.add(uid);
                    lobbyList.add(gameRoom.getGameID());

                    Lobby lobby = currentLobby.peek(task);
                    sendMulticastResponse(task, lobby.getUsers(), lobbyList,
                            lobby.getChannelID());

                    // send playerEnteredGame to game room
                    List grList = protocol.createCommandList(PLAYER_ENTERED_GAME);
                    grList.add(uid);
                    grList.add(userName);
                    
                    sendRetroJoins(task, gameRoom.getUsers(), PLAYER_ENTERED_GAME, 
                    				gameRoom.getChannelID());
                    
                    sendMulticastResponse(task, gameRoom.getUsers(), grList,
                            gameRoom.getChannelID());
                    
                    // send the 'ready' state (if true) of each player already connected
                    for (UserID curUser : gameRoom.getUsers()) {
                    	
                    	boolean isReady = gameRoom.isPlayerReady(curUser);
                    	if (curUser.equals(userID) || !isReady) {
                    		continue;
                    	}
	                    List list = protocol.createCommandList(PLAYER_READY_UPDATE);
	                    list.add(curUser);
	                    list.add(isReady);
	                    
	                    sendResponse(task, list, gameRoom.getChannelID());
                    }                    
                }

            }

        }

    }
    
    /**
     * <p>
     * Called when a user leaves a channel. There's only work to do if
     * the user leaving is the player. If so, they either left a lobby
     * or a game room (or the lobby manager control channel, which means
     * they left the server).
     * </p>
     * 
     * <p>
     * If they left a Lobby, simply remove them from the Lobby's list of
     * users.
     * </p>
     * 
     * <p>
     * If they left a Game Room, things are a bit more involved. The
     * player is removed from the Game Room's user list, and a message
     * is sent out to the Lobby that the player left the game, unless they left
     * because the game is about to start, in which case, no message is sent.
     * Additionally, if the player leaving is the game host, then the
     * game is shut down. Shutting down the game involves "leaving" all
     * the joined players (which results in more PlayerLeftGame messages
     * to the Lobby), and then sending out a GameDeleted message to the
     * Lobby.  In order to insure that the GameDeleted message is the last 
     * one sent out, the last player leaving the game room sends the message.
     * </p>
     * 
     * @see com.sun.gi.logic.SimUserDataListener#userLeftChannel
     */
    public void userLeftChannel(ChannelID cid, UserID uid) {
        SimTask task = SimTask.getCurrent();
        if (uid.equals(userID)) { // it was this player who left.
                                  
        	if (currentGameRoom != null) { // the player left a game room
                GameRoom gameRoom = (GameRoom) currentGameRoom.get(task);
                if (cid.equals(gameRoom.getChannelID())) { // user left
                                                            // from this
                                                            // channel
                	gameRoom.removeUser(uid);
                	Lobby lobby = currentLobby != null ? currentLobby.get(task) : null;
                	if (gameRoom.isStarting()) {
                		currentGameRoom = null;
                		return;
                	}

                    // if this was the host, kick everyone out and kill
                    // the game
                    if (gameRoom.getHost().equals(uid)) {
                        for (UserID curPlayer : gameRoom.getUsers()) {
                            task.leave(curPlayer, gameRoom.getChannelID());
                        }
                    }

                    // send PlayerLeftGame message to lobby
                    if (lobby != null) {
	                    List list = protocol.createCommandList(PLAYER_LEFT_GAME);
	                    list.add(uid);
	                    list.add(gameRoom.getGameID());
	
	                    sendMulticastResponse(task, lobby.getUsers(), list, lobby.getChannelID());
                    }
                    
                    List leftList = protocol.createCommandList(LEFT_GAME);
                    sendResponse(task, leftList);
                    
                    // If this is the last person leaving the game room, 
                    // send notification to the Lobby that the game was killed
                    // and remove the game from the lobby list
                    if (lobby != null && gameRoom.getUsers().size() == 0) {
                    	lobby.removeGameRoom(currentGameRoom);
                    	
                        List list = protocol.createCommandList(GAME_DELETED);
                        packGameDescriptor(list, gameRoom);

                        sendMulticastResponse(task, lobby.getUsers(), list, lobby.getChannelID());
                    }
                    
                    // currentGameRoom.delete(task);
                    currentGameRoom = null;
                    
                }
            } 
        	if (currentLobby != null) {
                Lobby lobby = (Lobby) currentLobby.get(task);
                if (cid.equals(lobby.getChannelID())) {
	                lobby.removeUser(uid);
	                // currentLobby.delete(task);
	                currentLobby = null;
	                
	                List leftList = protocol.createCommandList(LEFT_LOBBY);
	                sendResponse(task, leftList);
                }
            }
        }
    }

    public String getUserName() {
        return userName;
    }
    
    /**
     * Send a "catch-up" join message to the user for every player already on
     * the given channel.
     * 
     * @param users				the list of users
     * @param commandCode		the type of entry (lobby or game room)
     * @param cid				the ChannelID down which to send the message
     */
    private void sendRetroJoins(SimTask task, List<UserID> users, int commandCode, ChannelID cid) {
    	for (UserID curUser : users) {
    		if (curUser.equals(userID)) {
    			continue;
    		}
    		GLOReference<Player> pRef = task.findGLO(curUser.toString());
    		if (pRef == null) {
    			continue;
    		}
    		Player curPlayer = pRef.peek(task);
    		
    		List list = protocol.createCommandList(commandCode);
    		list.add(curUser);
    		list.add(curPlayer.getUserName());
    		
    		sendResponse(task, list, cid);
    	}
    	
    }

    /**
     * Responds to the ListFolderRequest command protocol. Reads the
     * requested FolderID off the buffer and attempts to find the folder
     * with the matching ID. If found, the subfolders of this folder are
     * listed as well as any lobbies.
     * 
     * @param task
     * @param data
     */
    private void listFolderRequest(SimTask task, ByteBuffer data) {
        SGSUUID folderID = null;
        if (data.hasRemaining()) {
            folderID = protocol.readUUID(data);
        }
        Folder root = (Folder) folderRoot.peek(task);
        Folder targetFolder = folderID == null ? root : root.findFolder(task,
                folderID);
        System.out.println("folderID = " + folderID + " targetFolder "
                + targetFolder.getName());
        List list = protocol.createCommandList(LIST_FOLDER_RESPONSE);
        list.add(folderID == null ? root.getFolderID() : folderID);
        if (targetFolder != null) {
            list.add(targetFolder.getFolders().size());
            for (GLOReference<Folder> folderRef : targetFolder.getFolders()) {
                Folder curFolder = folderRef.peek(task);

                // list out the contents of the current folder
                list.add(curFolder.getName());
                list.add(curFolder.getDescription());
                list.add(curFolder.getFolderID());

            }

            // finally list out the lobbies.
            List<GLOReference<Lobby>> lobbyList = targetFolder.getLobbies();
            list.add(lobbyList.size());
            for (GLOReference<Lobby> lobbyRef : lobbyList) {
                Lobby curLobby = lobbyRef.peek(task);

                list.add(curLobby.getName());
                list.add(curLobby.getChannelName());
                list.add(curLobby.getDescription());
                list.add(curLobby.getLobbyID());
                list.add(curLobby.getNumPlayers());
                list.add(curLobby.getMaxPlayers());
                list.add(curLobby.isPasswordProtected());
            }
        } else { // return a zero size
            list.add(0);
        }
        sendResponse(task, list);
    }

    /**
     * Attempts to lookup a connected Player by their user name. The
     * Player's ID is returned.
     * 
     * @param task
     * @param data
     */
    private void lookupUserIDRequest(SimTask task, ByteBuffer data) {
        String username = protocol.readString(data);
        UserID id = null;
        if (username != null) {
            GLOMap<String, UserID> userMap = (GLOMap<String, UserID>) task.findGLO(
                    "UsernameMap").peek(task);
            id = userMap.get(username);
        }

        List list = protocol.createCommandList(LOOKUP_USER_ID_RESPONSE);
        list.add(username);
        list.add(id);

        sendResponse(task, list);
    }

    private void lookupUserNameRequest(SimTask task, ByteBuffer data) {
        UserID id = protocol.readUserID(data);
        String username = null;
        if (id != null) {
            GLOReference<Player> pRef = task.findGLO(id.toString());
            if (pRef != null) {
                Player player = pRef.get(task);
                username = player.getUserName();
            }
        }

        List list = protocol.createCommandList(LOOKUP_USER_NAME_RESPONSE);
        list.add(username);
        list.add(id);

        sendResponse(task, list);
    }

    /**
     * Attempts to find the lobby that the requested user is connected
     * to if any.
     * 
     * @param task the SimTask
     * @param data the data buffer
     */
    private void locateUserRequest(SimTask task, ByteBuffer data) {
        UserID requestedID = protocol.readUserID(data);
        GLOReference<Player> requestedRef = task.findGLO(requestedID.toString());
        Lobby lobby = null;
        if (requestedRef != null) {
            Player requestedPlayer = requestedRef.peek(task);
            if (requestedPlayer.getCurrentLobby() != null) {
                lobby = requestedPlayer.getCurrentLobby().peek(task);
            }
        }
        List list = protocol.createCommandList(LOCATE_USER_RESPONSE);
        list.add(requestedID);
        list.add(lobby != null ? 1 : 0); // number of lobbies -- only
                                            // one allowed
        if (lobby != null) {
            list.add(lobby.getName());
            list.add(lobby.getDescription());
            list.add(lobby.getLobbyID());
            list.add(lobby.getNumPlayers());
            list.add(lobby.getMaxPlayers());
            list.add(lobby.isPasswordProtected());
        }
        sendResponse(task, list);
    }
    
    private void leaveLobby(SimTask task) {
    	if (currentLobby == null) {
    		sendErrorResponse(task, "Not connected to a lobby.");
    	}
    	Lobby lobby = currentLobby.peek(task);
    	task.leave(userID, lobby.getChannelID());
    }
    
    private void leaveGame(SimTask task) {
    	if (currentGameRoom == null) {
    		sendErrorResponse(task, "Not connected to a game.");
    	}
    	GameRoom game = currentGameRoom.peek(task);
    	task.leave(userID, game.getChannelID());
    }

    /**
     * Attempts to join this user to the Lobby channel specified in the
     * data buffer. If the lobby is password protected, then a password
     * is read off the buffer and compared.
     * 
     * @param task the SimTask
     * @param data the buffer containing the command parameters
     */
    private void joinLobby(SimTask task, ByteBuffer data) {
        System.out.println("joinLobby request");
        if (currentLobby != null) {
            sendErrorResponse(task, "Already connected to a lobby");
            return;
        }

        SGSUUID lobbyID = protocol.readUUID(data);
        GLOMap<SGSUUID, GLOReference<Lobby>> lobbyMap =
            (GLOMap<SGSUUID, GLOReference<Lobby>>) task.findGLO(
                "LobbyMap").peek(task);
        GLOReference<Lobby> lobbyRef = lobbyMap.get(lobbyID);
        if (lobbyRef != null) {
            Lobby lobby = lobbyRef.peek(task);
            if (lobby.getNumPlayers() == lobby.getMaxPlayers()) {
                // TODO sten: perhaps in the future spawn a new lobby.
            	sendErrorResponse(task, "Lobby at max users");
                return;
            }
            if (lobby.isPasswordProtected()) {
                String password = protocol.readString(data);
                if (lobby.getPassword().equals(password)) {
                    task.join(userID, lobby.getChannelID());
                } else {
                    sendErrorResponse(task, "password mismatch for lobbyID "
                            + lobbyID);
                    return;
                }
            } else {
                task.join(userID, lobby.getChannelID());
            }

        } else {
            sendErrorResponse(task, "lobby not found: " + lobbyID);
        }
    }

    /**
     * Sends a response of ERROR on the control channel with a message
     * detailing the failure.
     * 
     * @param message the reason for the error
     */
    private void sendErrorResponse(SimTask task, String message) {
        List list = protocol.createCommandList(ERROR);
        list.add(message);
        sendResponse(task, list);
    }

    private void startGame(SimTask task) {
        if (currentGameRoom == null) {
        	sendErrorResponse(task, "Start failed.  Not connected to a game room.");
            return;
        }
        if (currentLobby == null) {
        	sendErrorResponse(task, "Start failed.  Not connected to a lobby.");
        }
        GameRoom gameRoom = currentGameRoom.get(task);
        UserID host = gameRoom.getHost();
        // only the host can start the game.
        if (!host.equals(userID)) {
            sendStartGameFailed(task, "Only the host can start a game",
                    gameRoom.getChannelID());
            return;
        }
        if (!gameRoom.arePlayersReady()) {
            sendStartGameFailed(task, "Not all players are ready",
                    gameRoom.getChannelID());
            return;
        }

        gameRoom.setStarting(true);
        Lobby lobby = currentLobby.get(task);
        lobby.removeGameRoom(currentGameRoom);
        
        List list = protocol.createCommandList(GAME_STARTED);
        packGameDescriptor(list, gameRoom);

        // send identical message to both game room and lobby
        sendMulticastResponse(task, gameRoom.getUsers(), list, gameRoom.getChannelID());
        sendMulticastResponse(task, lobby.getUsers(), list, lobby.getChannelID());
        
        // unjoin all game players from the lobby.
        for(UserID curUser : gameRoom.getUsers()) {
        	task.leave(curUser, lobby.getChannelID());
        	task.leave(curUser, gameRoom.getChannelID());
        }
    }
    
    private void sendStartGameFailed(SimTask task, String reason, ChannelID cid) {
        List list = protocol.createCommandList(START_GAME_REQUEST);
        list.add(reason);

        sendResponse(task, list, cid);
    }

    private void updatePlayerReadyRequest(SimTask task, ByteBuffer data) {
        if (currentGameRoom == null) {
        	sendErrorResponse(task, "Not connected to a game room");
            return;
        }
        boolean ready = protocol.readBoolean(data);
        GameRoom gameRoom = currentGameRoom.get(task);
        if (ready) {
            int numParams = data.getInt();
            HashMap<String, Object> gameParams = new HashMap<String, Object>();
            for (int i = 0; i < numParams; i++) {
                gameParams.put(protocol.readString(data),
                        protocol.readParamValue(data));
            }

            Map<String, Object> masterParameters = gameRoom.getGameParamters();

            // bail out if all of the parameters are not equal.
            if (!gameParams.equals(masterParameters)) {
                sendPlayerReadyUpdate(task, false, gameRoom);
                return;
            }

            // all criteria has been met, the play is indeed ready (or
            // not).
            gameRoom.updateReady(userID, ready);
        }
        sendPlayerReadyUpdate(task, ready, gameRoom);
    }

    private void sendPlayerReadyUpdate(SimTask task, boolean ready, GameRoom game) {
        List list = protocol.createCommandList(PLAYER_READY_UPDATE);
        list.add(userID);
        list.add(ready);

        sendMulticastResponse(task, game.getUsers(), list, game.getChannelID());
    }

    /**
     * Attempts to join this user to the Game Room channel specified in
     * the data buffer. If the game is password protected, then a
     * password is read off the buffer and compared.
     * 
     * @param task the SimTask
     * @param data the buffer containing the command parameters
     */
    private void joinGame(SimTask task, ByteBuffer data) {
        if (currentGameRoom != null) { // can't connect if already
                                        // connected to a game.
        	sendErrorResponse(task, "Already connected to a game room");
            return;
        }
        SGSUUID gameID = protocol.readUUID(data);

        GLOMap<SGSUUID, GLOReference<GameRoom>> gameRoomMap =
            (GLOMap<SGSUUID, GLOReference<GameRoom>>) task.findGLO(
                "GameRoomMap").peek(task);
        GLOReference<GameRoom> gameRef = gameRoomMap.get(gameID);
        if (gameRef == null) {
        	sendErrorResponse(task, "Requested game room does not exist");
            return;
        }
        GameRoom gameRoom = gameRef.peek(task);
        if (gameRoom.isPasswordProtected()) {
            String password = protocol.readString(data);
            if (!password.equals(gameRoom.getPassword())) {
            	sendErrorResponse(task, "Incorrect password for Game");
                return;
            }
        }
        task.join(userID, gameRoom.getChannelID());
    }

    /**
     * Called when a user requests the parameters for a game on the
     * currently connected lobby. The GameParametersResponse command is
     * sent as the response.
     * 
     * @param task the SimTask
     */
    private void gameParametersRequest(SimTask task) {
        List list = protocol.createCommandList(GAME_PARAMETERS_RESPONSE);
        if (currentLobby != null) {
            Lobby lobby = currentLobby.peek(task);
            list.add(lobby.getChannelName());
            Map<String, Object> gameParameters = lobby.getGameParamters();
            list.add(gameParameters.size());
            for (String curKey : gameParameters.keySet()) {
                list.add(curKey);
                Object value = gameParameters.get(curKey);
                System.out.println("adding param " + curKey + " value " + value);
                list.add(protocol.mapType(value));
                list.add(value);

            }
        } else {
            sendErrorResponse(task, "Not connected to a lobby");
            return;
        }

        sendResponse(task, list);
    }

    /**
     * <p>
     * Processes a request to create a new game room in the user's
     * current lobby. If the game can not be created for any reason, a
     * CreateGameFailed response is sent back to the user with a reason
     * for the failure.
     * </p>
     * 
     * <p>
     * If creation is successful, the new GameRoom object is added to
     * the lobby and connected users are notified via the GameCreated
     * response.
     * </p>
     * 
     * @param task the SimTask
     * @param data the buffer containing the request data
     */
    private void createGame(SimTask task, ByteBuffer data) {
        String gameName = protocol.readString(data);
        if (currentLobby == null) { // bail out early if not connected.
            sendGameCreateFailedResponse(task, null, gameName, "Not connected to a lobby");
            return;
        }

        String description = protocol.readString(data);
        boolean hasPassword = protocol.readBoolean(data);
        String password = null;
        if (hasPassword) {
            password = protocol.readString(data);
        }

        int numParams = data.getInt();
        HashMap<String, Object> gameParams = new HashMap<String, Object>();
        for (int i = 0; i < numParams; i++) {
            gameParams.put(protocol.readString(data),
                    protocol.readParamValue(data));
        }

        Lobby lobby = currentLobby.get(task);
        Map<String, Object> lobbyParameters = lobby.getGameParamters();

        // bail out if all of the expected parameters are not present.
        if (!gameParams.keySet().equals(lobbyParameters.keySet())) {
            sendGameCreateFailedResponse(task, lobby.getChannelName(),
                    gameName, "Parameters mis-match.");
            return;
        }

        String channelName = lobby.getChannelName() + ":" + gameName;
        ChannelID cid = task.openChannel(channelName);
        task.lock(cid, true); // game access is controled by the server

        GLOReference<GameRoom> grRef = task.createGLO(new GameRoom(gameName,
                description, password, channelName, cid, userID));
        lobby.addGameRoom(grRef);

        GameRoom gr = grRef.get(task);

        // add to the game room map for easy look-up by gameID
        GLOMap<SGSUUID, GLOReference<GameRoom>> gameRoomMap =
            (GLOMap<SGSUUID, GLOReference<GameRoom>>) task.findGLO(
                "GameRoomMap").get(task);
        gameRoomMap.put(gr.getGameID(), grRef);

        for (Map.Entry<String, Object> entry : gameParams.entrySet()) {
            gr.addGameParameter(entry.getKey(), entry.getValue());
        }

        List list = protocol.createCommandList(GAME_CREATED);
        packGameDescriptor(list, gr);

        sendMulticastResponse(task, lobby.getUsers(), list, lobby.getChannelID());

        // join the user that created this game as the host after the
        // CREATE_GAME message has been sent.
        task.join(userID, cid);
    }

    /**
     * Puts the details of a game room into the given list for
     * transport.
     * 
     * @param list the list in which to put the game details
     * @param game the game
     */
    private void packGameDescriptor(List list, GameRoom game) {
        list.add(game.getChannelID());
        list.add(game.getName());
        list.add(game.getDescription());
        list.add(game.getChannelName());
        list.add(game.isPasswordProtected());

        Map<String, Object> gameParams = game.getGameParamters();
        list.add(gameParams.size());

        for (Map.Entry<String, Object> entry : gameParams.entrySet()) {
            String curKey = entry.getKey();
            list.add(curKey);
            
            Object value = entry.getValue();
            list.add(protocol.mapType(value));
            list.add(value);
        }
    }

    private void sendGameCreateFailedResponse(SimTask task,
            String lobbyChannelName, String gameName, String message) {
    	
        List list = protocol.createCommandList(CREATE_GAME_FAILED);
        list.add(gameName);
        list.add(message);
        list.add(lobbyChannelName);

        sendResponse(task, list);
    }

    private void sendResponse(SimTask task, List list) {
        ChannelID cid = task.openChannel(LOBBY_MANAGER_CONTROL_CHANNEL);

        sendResponse(task, list, cid);
    }

    private void sendMulticastResponse(SimTask task, List<UserID> users,
            List list, ChannelID cid) {
    	
        sendResponse(task, users.toArray(new UserID[users.size()]), list, cid);
    }

    private void sendResponse(SimTask task, List list, ChannelID cid) {
        sendResponse(task, new UserID[] { userID }, list, cid);
    }

    private void sendResponse(SimTask task, UserID[] to, List list,
            ChannelID cid) {
        ByteBuffer data = protocol.assembleCommand(list);

        task.sendData(cid, to, data, true);
    }

}
