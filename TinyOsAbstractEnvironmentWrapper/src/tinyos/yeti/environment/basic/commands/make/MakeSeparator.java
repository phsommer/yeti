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

import tinyos.yeti.environment.basic.AbstractEnvironment;
import tinyos.yeti.environment.basic.commands.AbstractCommand;
import tinyos.yeti.environment.basic.commands.IExecutionResult;

public class MakeSeparator extends AbstractCommand<String>{
    private AbstractEnvironment environment;

    public MakeSeparator( String platform, AbstractEnvironment environment ){
        this.environment = environment;

        setCommand( "make", platform, "-n" );
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

    public String result( IExecutionResult result ){
    	String sep = parseMakeOutputString( result.getOutput() );
        return sep;
    }

    public boolean shouldPrintSomething(){
        return true;
    }

    //Example output: 
    //mkdir -p build/meshbean900
    //echo '-DIDENT_USERNAME=\"dcg\" -DIDENT_HOSTNAME=\"pc-10109\" -DIDENT_USERHASH=0x6d2cac77L -DIDENT_TIMESTAMP=0x49f729d6L -DIDENT_UIDHASH=0xab60e380L' > build/meshbean900/ident_flags.txt
    //echo "    compiling  to a meshbean900 binary"
    //ncc -o build/meshbean900/main.exe  -Os -fnesc-separator=__ -Wall -Wshadow -Wnesc-all -target=meshbean900 -fnesc-cfile=build/meshbean900/app.c -board= -DDEFINED_TOS_AM_GROUP=0x22 --param max-inline-insns-single=100000 -DIDENT_USERNAME=\"dcg\" -DIDENT_HOSTNAME=\"pc-10109\" -DIDENT_USERHASH=0x6d2cac77L -DIDENT_TIMESTAMP=0x49f729d6L -DIDENT_UIDHASH=0xab60e380L -fnesc-dump=wiring -fnesc-dump='interfaces(!abstract())' -fnesc-dump='referenced(interfacedefs, components)' -fnesc-dump=variables -fnesc-dumpfile=build/meshbean900/wiring-check.xml .nc -lm 
    //nescc-wiring build/meshbean900/wiring-check.xml
    //echo "    compiled  to build/meshbean900/main.exe"
    //avr-objdump -h build/meshbean900/main.exe | perl -ne '$b{$1}=hex $2 if /^\s*\d+\s*\.(text|data|bss)\s+(\S+)/; END { printf("%16d bytes in ROM\n%16d bytes in RAM\n",$b{text}+$b{data},$b{data}+$b{bss}); }'
    //:
    //avr-objcopy --output-target=srec build/meshbean900/main.exe build/meshbean900/main.srec
    //avr-objcopy --output-target=ihex build/meshbean900/main.exe build/meshbean900/main.ihex
    //echo "    writing TOS image"
    //tos-write-image -DIDENT_USERNAME=\"dcg\" -DIDENT_HOSTNAME=\"pc-10109\" -DIDENT_USERHASH=0x6d2cac77L -DIDENT_TIMESTAMP=0x49f729d6L -DIDENT_UIDHASH=0xab60e380L --ihex="build/meshbean900/main.ihex" --exe="build/meshbean900/main.exe" --objdump="avr-objdump" --platform="meshbean900" > build/meshbean900/tos_image.xml
    //:
    //:

    private String parseMakeOutputString(String input) {
        if (input.indexOf("does not specify a valid target.") != -1) {
            return null;
        } 
        
        if (input.indexOf("No rule to make target") != -1) {
            return null;
        } 

        String beginDelimiter = "-fnesc-separator=";
        String endDelimiter = " ";
        
        int beginIndex = input.indexOf(beginDelimiter);
        if (beginIndex == -1) return null;
        beginIndex += beginDelimiter.length();
        int endIndex = input.indexOf(endDelimiter, beginIndex);
        if ( endIndex == -1)return null;

        String separator = input.substring(beginIndex, endIndex);

        return separator;
    }

}
