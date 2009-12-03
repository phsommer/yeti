/*
 * Yeti 2, NesC development in Eclipse.
 * Copyright (C) 2009 ETH Zurich
 *
 * This program is free software: you can redistribute it and/or modify
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
 *
 * Web:  http://tos-ide.ethz.ch
 * Mail: tos-ide@tik.ee.ethz.ch
 */
package tinyos.yeti.environment.winXP.platform;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.environment.basic.path.IPathTranslator;
import tinyos.yeti.environment.basic.platform.AbstractPlatform;
import tinyos.yeti.environment.basic.platform.MMCUConverter;
import tinyos.yeti.environment.basic.platform.SensorBoard;
import tinyos.yeti.environment.winXP.Environment;
import tinyos.yeti.ep.ISensorBoard;

public class Platform extends AbstractPlatform{
    private Environment environment;
    private IPathTranslator translator;
    
    public Platform( Environment environment, IPathTranslator translator, File directory, File top, MMCUConverter converter ){
        super( environment, directory, top, converter );

        if( translator == null )
            translator = environment;
        
        this.environment = environment;
        this.translator = translator;
    }
    
    @Override
    protected ISensorBoard[] loadSensorBoards(){
        File directory = translator.modelToSystem( environment.getPathManager().getSensorPath() );
        if( directory == null )
        	return new ISensorBoard[]{};
        
        List<ISensorBoard> result = new ArrayList<ISensorBoard>();
        loadSensorBoard( result, directory );
        
        return result.toArray( new ISensorBoard[ result.size() ] );
    }

    private void loadSensorBoard( List<ISensorBoard> boards, File directory ){
        File sensor = new File( directory, ".sensor" );
        if( sensor.exists() ){
            SensorBoard board = new SensorBoard();
            board.setName( directory.getName() );
            board.setDirectory( directory );
            boards.add( board );
        }
        if( directory.isDirectory() ){
            File[] children = directory.listFiles();
            if( children != null ){
                for( File child : children ){
                    loadSensorBoard( boards, child );
                }
            }
        }
    }
}
