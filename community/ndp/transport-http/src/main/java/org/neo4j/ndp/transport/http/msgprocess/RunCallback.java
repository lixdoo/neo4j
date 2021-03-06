/**
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.ndp.transport.http.msgprocess;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.kernel.impl.util.StringLogger;
import org.neo4j.ndp.runtime.StatementMetadata;

public class RunCallback extends MessageProcessingCallback<StatementMetadata>
{
    private final Map<String, Object> successMetadata = new HashMap<>();

    public RunCallback( StringLogger log )
    {
        super(log);
    }

    @Override
    public void result( StatementMetadata result, Void none ) throws Exception
    {
        successMetadata.put( "fields", result.fieldNames() );
    }

    @Override
    protected Map successMetadata()
    {
        return successMetadata;
    }
}
