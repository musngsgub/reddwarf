/*
 * Copyright 2008 Sun Microsystems, Inc.
 *
 * This file is part of the Darkstar Test Cluster
 *
 * Darkstar Test Cluster is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation and
 * distributed hereunder to you.
 *
 * Darkstar Test Cluster is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.projectdarkstar.tools.dtc.data;

import com.projectdarkstar.tools.dtc.service.DTCInvalidDataException;

/**
 * Represents a snapshot of the number of clients in the system at runtime
 * at a specific point in time during the execution for a specific type
 * of {@link ClientAppConfigDTO}.
 */
public class TestExecutionResultClientDataTupleDTO extends AbstractDTO
{
    private Long id;
    private String originalClientName;
    private Long numClients;
    private ClientAppConfigDTO client;
    
    private TestExecutionResultClientDataDTO clientData;
    
    public TestExecutionResultClientDataTupleDTO(ClientAppConfigDTO client,
                                                 String originalClientName,
                                                 Long numClients)
    {
        this.setClient(client);
        this.setOriginalClientName(originalClientName);
        this.setNumClients(numClients);
    }
    
    /**
     * Returns the id of the entity in persistent storage
     * 
     * @return id of the entity
     */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOriginalClientName() { return originalClientName; }
    private void setOriginalClientName(String originalClientName) { this.originalClientName = originalClientName; }
    
    public Long getNumClients() { return numClients; }
    protected void setNumClients(Long numClients) { this.numClients = numClients; }
    public void updateNumClients(Long numClients)
            throws DTCInvalidDataException {
        this.updateAttribute("numClients", numClients);
    }
    
    public ClientAppConfigDTO getClient() { return client; }
    protected void setClient(ClientAppConfigDTO client) { this.client = client; }
    public void updateClient(ClientAppConfigDTO client)
            throws DTCInvalidDataException {
        this.updateAttribute("client", client);
    }
    
    public TestExecutionResultClientDataDTO getClientData() { return clientData; }
    protected void setClientData(TestExecutionResultClientDataDTO clientData) { this.clientData = clientData; }
    public void updateClientData(TestExecutionResultClientDataDTO clientData)
            throws DTCInvalidDataException {
        this.updateAttribute("clientData", clientData);
    }

    /** @inheritDoc */
    public void validate() throws DTCInvalidDataException {}
}
