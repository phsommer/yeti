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
package tinyos.yeti.environment.basic.commands.make;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tinyos.yeti.environment.basic.AbstractEnvironment;
import tinyos.yeti.environment.basic.commands.AbstractCommand;
import tinyos.yeti.environment.basic.commands.IExecutionResult;
import tinyos.yeti.ep.IMakeExtraDescription;
import tinyos.yeti.ep.MakeExtraDescription;

public class MakeExtras extends AbstractCommand<IMakeExtraDescription[]>{
    private AbstractEnvironment environment;

    public MakeExtras( String platform, AbstractEnvironment environment ){
        this.environment = environment;

        setCommand( "make", platform + " help" );
    }

    @Override
    public boolean setup(){
        String rules = environment.getPathManager().getMakerulesPath();
        if( rules == null )
            return false;
        
        File file = environment.modelToSystem( rules );
        if( file == null || !file.exists() )
            return false;
        
        setDirectory( file.getParentFile() );
      
        return true;
    }

    public IMakeExtraDescription[] result( IExecutionResult result ){
    	IMakeExtraDescription[] me = parseExtraString( result.getError() );
        if( me == null )
            return null;

        Arrays.sort( me,new Comparator<IMakeExtraDescription>(){
            public int compare( IMakeExtraDescription a, IMakeExtraDescription b ) {
                String nameA = a.getName();
                String nameB = b.getName();

                return String.CASE_INSENSITIVE_ORDER.compare( nameA, nameB );
            }
        });

        return me;
    }

    public boolean shouldPrintSomething(){
        return true;
    }

    //Example output: 
    ///opt/tinyos-1.x/tools/make/Makerules:178: ***
    //
    //Usage:  make mica <extras>
    //Valid targets: all cc2420dbk clean imote2 mica mica2 
    //mica2dot micaz pc
    //eset snms_schema telos telos_hc08 telosa telosb tmote
    //Valid extras: avrisp dapa debug debugopt deps docs eprb ident_flags ins
    //all mib510 msg reinstall tinysec tos_image xnp
    //
    // Welcome to the TinyOS make system!
    //
    // You must specify one of the valid targets and possibly some combination of
    // the extra options.  Many targets have custom extras and extended help, so be
    // sure to try "make <target> help" to learn of all the available features.
    //
    // Global extras:
    //
    //   docs    : compile additional nescdoc documentation
    //   tinysec : compile with TinySec secure communication
    //
    // AVR extras:
    //
    //   debug    : compile with minimal optimization and debug symbols
    //   debugopt : compile with debug symbols
    //   xnp      : compile for network programming
    //
    // Programmer options:
    //
    //   dapa         : (default) use parallel port programmer
    //   mib510,<dev> : use MIB510 serial port programming board at port <dev>
    //   eprb,<host>  : use EPRB at hostname <host>
    //   avrisp,<dev> : use AVRISP serial programmer at port <dev>
    //
    //   The dev or host parameter for the programmer option need not be specified,
    //   in which case it is expected to be defined as in an environment variable of
    //   the same name in all caps (such as MIB510, EPRB, or AVRISP).
    //
    //  Thank you.  Stop.

    private IMakeExtraDescription[] parseExtraString(String input) {
        //java.lang.System.out.println("input -"+input);

        if (input.indexOf("does not specify a valid target.") != -1) {
            return null;
        } 

        ArrayList<IMakeExtraDescription> makeExtras = new ArrayList<IMakeExtraDescription>();

        String beginDelimiter = "Valid extras:";
        String endDelimiter = "Welcome to the";
        if (input.indexOf(beginDelimiter) == -1) return null;
        if (input.indexOf(endDelimiter) == -1)return null;

        String extra = input.substring(input.indexOf(beginDelimiter)+beginDelimiter.length()+1,
                input.indexOf(endDelimiter));

        String[] extraNames = extra.split(" ");

        // trim list of any whitespace character
        for (int i = 0; i < extraNames.length; i++) {
            extraNames[i] = extraNames[i].trim();
        }

        List<String> extraNamesList = Arrays.asList(extraNames);
        Map<String,String> addInfo = new HashMap<String,String>();
        Map<String,String> addParam = new HashMap<String,String>();

        // parse extra options
        String[] i2 = input.split("\n");
        for (int i = 0; i < i2.length; i++) {
            String[] components = i2[i].split(":");
            if (components.length < 2)
            	continue;

            String temp = components[0].trim();
            //System.out.println("[0] -> "+temp);
            if (extraNamesList.contains(temp)) {
                // then the next array position holds additional information..

                //System.out.println("Type:\""+temp+"\" info "+components[1].trim());
                addInfo.put(temp,components[1].trim());
            } 
            else {
                // could be extra with parameter: example mib510,<dev>
                String extr = temp.split(",")[0].trim();
                if (extraNamesList.contains(extr)) {

                    //System.out.println("Type:\""+extr+"\" info "+components[1].trim());
                    addInfo.put(extr,components[1].trim());
                    addParam.put(extr,temp.split(",")[1].trim());
                }
            }
        }

        for (int i = 0; i < extraNames.length ; i++) {
        	MakeExtraDescription m = new MakeExtraDescription( extraNames[i] );
            if( extraNames[i].equals("install") ){
                m.setDescription( "Compile the application for the target platform, set the address and program the device." );
                m.setParameterName( "Mote Id" );
                m.setParameterDescription( "unique node identification number." ); 
            }
            else if( extraNames[i].equals("reinstall") ){
                m.setDescription( "Set the address and program the device ONLY (does not recompile). This option is significantly faster then install." );
                m.setParameterName( "Mote Id" );
                m.setParameterDescription( "unique node identification number." ); 
            }
            else{
                m.setDescription( addInfo.get( extraNames[i] ) );
                m.setParameterName( addParam.get( extraNames[i] ) );
            }

            makeExtras.add( m );      
        }

        return makeExtras.toArray(new IMakeExtraDescription[makeExtras.size()]);
    }

}
