/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.posix.commands;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.posix.commands.chmod.Chmod.Changer;
import org.xmlsh.posix.commands.ls.ListVisitor;
import org.xmlsh.util.FileUtils;
import org.xmlsh.util.IPathTreeVisitor;
import org.xmlsh.util.PathMatchOptions;
import org.xmlsh.util.UnifiedFileAttributes;
import org.xmlsh.util.Util;
import org.xmlsh.util.XFile;





import java.nio.file.*;
import java.nio.file.attribute.*;

import static java.nio.file.attribute.PosixFilePermission.*;
import static java.nio.file.FileVisitResult.*;
import static org.xmlsh.util.UnifiedFileAttributes.MatchFlag.HIDDEN_NAME;
import static org.xmlsh.util.UnifiedFileAttributes.MatchFlag.HIDDEN_SYS;
import static org.xmlsh.util.UnifiedFileAttributes.MatchFlag.SYSTEM;

import java.io.IOException;
import java.util.*;

public class chmod extends XCommand {
    
    // From : http://docs.oracle.com/javase/tutorial/displayCode.html?code=http://docs.oracle.com/javase/tutorial/essential/io/examples/Chmod.java
    
    static class Chmod {
        
        
        /**
         * Compiles a list of one or more <em>symbolic mode expressions</em> that
         * may be used to change a set of file permissions. This method is
         * intended for use where file permissions are required to be changed in
         * a manner similar to the UNIX <i>chmod</i> program.
         *
         * <p> The {@code exprs} parameter is a comma separated list of expressions
         * where each takes the form:
         * <blockquote>
         * <i>who operator</i> [<i>permissions</i>]
         * </blockquote>
         * where <i>who</i> is one or more of the characters {@code 'u'}, {@code 'g'},
         * {@code 'o'}, or {@code 'a'} meaning the owner (user), group, others, or
         * all (owner, group, and others) respectively.
         *
         * <p> <i>operator</i> is the character {@code '+'}, {@code '-'}, or {@code
         * '='} signifying how permissions are to be changed. {@code '+'} means the
         * permissions are added, {@code '-'} means the permissions are removed, and
         * {@code '='} means the permissions are assigned absolutely.
         *
         * <p> <i>permissions</i> is a sequence of zero or more of the following:
         * {@code 'r'} for read permission, {@code 'w'} for write permission, and
         * {@code 'x'} for execute permission. If <i>permissions</i> is omitted
         * when assigned absolutely, then the permissions are cleared for
         * the owner, group, or others as identified by <i>who</i>. When omitted
         * when adding or removing then the expression is ignored.
         *
         * <p> The following examples demonstrate possible values for the {@code
         * exprs} parameter:
         *
         * <table border="0">
         * <tr>
         *   <td> {@code u=rw} </td>
         *   <td> Sets the owner permissions to be read and write. </td>
         * </tr>
         * <tr>
         *   <td> {@code ug+w} </td>
         *   <td> Sets the owner write and group write permissions. </td>
         * </tr>
         * <tr>
         *   <td> {@code u+w,o-rwx} </td>
         *   <td> Sets the owner write, and removes the others read, others write
         *     and others execute permissions. </td>
         * </tr>
         * <tr>
         *   <td> {@code o=} </td>
         *   <td> Sets the others permission to none (others read, others write and
         *     others execute permissions are removed if set) </td>
         * </tr>
         * </table>
         *
         * @param   exprs
         *          List of one or more <em>symbolic mode expressions</em>
         *
         * @return  A {@code Changer} that may be used to changer a set of
         *          file permissions
         *
         * @throws  IllegalArgumentException
         *          If the value of the {@code exprs} parameter is invalid
         */
        public static Changer compile(String exprs) {
            // minimum is who and operator (u= for example)
            if (exprs.length() < 2)
                throw new IllegalArgumentException("Invalid mode");
     
            // permissions that the changer will add or remove
            final Set<PosixFilePermission> toAdd = new HashSet<PosixFilePermission>();
            final Set<PosixFilePermission> toRemove = new HashSet<PosixFilePermission>();
     
            // iterate over each of expression modes
            for (String expr: exprs.split(",")) {
                // minimum of who and operator
                if (expr.length() < 2)
                    throw new IllegalArgumentException("Invalid mode");
     
                int pos = 0;
     
                // who
                boolean u = false;
                boolean g = false;
                boolean o = false;
                boolean done = false;
                for (;;) {
                    switch (expr.charAt(pos)) {
                        case 'u' : u = true; break;
                        case 'g' : g = true; break;
                        case 'o' : o = true; break;
                        case 'a' : u = true; g = true; o = true; break;
                        default : done = true;
                    }
                    if (done)
                        break;
                    pos++;
                }
                
                if (!u && !g && !o){
                    if( done && pos == 0 ){
                        u = true; g = true; o = true; 
                    }
                    else
                    throw new IllegalArgumentException("Invalid mode");
                }
                // get operator and permissions
                char op = expr.charAt(pos++);
                String mask = (expr.length() == pos) ? "" : expr.substring(pos);
     
                // operator
                boolean add = (op == '+');
                boolean remove = (op == '-');
                boolean assign = (op == '=');
                if (!add && !remove && !assign)
                    throw new IllegalArgumentException("Invalid mode");
     
                // who= means remove all
                if (assign && mask.length() == 0) {
                    assign = false;
                    remove = true;
                    mask = "rwx";
                }
     
                // permissions
                boolean r = false;
                boolean w = false;
                boolean x = false;
                for (int i=0; i<mask.length(); i++) {
                    switch (mask.charAt(i)) {
                        case 'r' : r = true; break;
                        case 'w' : w = true; break;
                        case 'x' : x = true; break;
                        default:
                            throw new IllegalArgumentException("Invalid mode");
                    }
                }
     
                // update permissions set
                if (add) {
                    if (u) {
                        if (r) toAdd.add(OWNER_READ);
                        if (w) toAdd.add(OWNER_WRITE);
                        if (x) toAdd.add(OWNER_EXECUTE);
                    }
                    if (g) {
                        if (r) toAdd.add(GROUP_READ);
                        if (w) toAdd.add(GROUP_WRITE);
                        if (x) toAdd.add(GROUP_EXECUTE);
                    }
                    if (o) {
                        if (r) toAdd.add(OTHERS_READ);
                        if (w) toAdd.add(OTHERS_WRITE);
                        if (x) toAdd.add(OTHERS_EXECUTE);
                    }
                }
                if (remove) {
                    if (u) {
                        if (r) toRemove.add(OWNER_READ);
                        if (w) toRemove.add(OWNER_WRITE);
                        if (x) toRemove.add(OWNER_EXECUTE);
                    }
                    if (g) {
                        if (r) toRemove.add(GROUP_READ);
                        if (w) toRemove.add(GROUP_WRITE);
                        if (x) toRemove.add(GROUP_EXECUTE);
                    }
                    if (o) {
                        if (r) toRemove.add(OTHERS_READ);
                        if (w) toRemove.add(OTHERS_WRITE);
                        if (x) toRemove.add(OTHERS_EXECUTE);
                    }
                }
                if (assign) {
                    if (u) {
                        if (r) toAdd.add(OWNER_READ);
                          else toRemove.add(OWNER_READ);
                        if (w) toAdd.add(OWNER_WRITE);
                          else toRemove.add(OWNER_WRITE);
                        if (x) toAdd.add(OWNER_EXECUTE);
                          else toRemove.add(OWNER_EXECUTE);
                    }
                    if (g) {
                        if (r) toAdd.add(GROUP_READ);
                          else toRemove.add(GROUP_READ);
                        if (w) toAdd.add(GROUP_WRITE);
                          else toRemove.add(GROUP_WRITE);
                        if (x) toAdd.add(GROUP_EXECUTE);
                          else toRemove.add(GROUP_EXECUTE);
                    }
                    if (o) {
                        if (r) toAdd.add(OTHERS_READ);
                          else toRemove.add(OTHERS_READ);
                        if (w) toAdd.add(OTHERS_WRITE);
                          else toRemove.add(OTHERS_WRITE);
                        if (x) toAdd.add(OTHERS_EXECUTE);
                          else toRemove.add(OTHERS_EXECUTE);
                    }
                }
            }
     
            // return changer
            return new Changer() {
                @Override
                public Set<PosixFilePermission> change( Set<PosixFilePermission> posix) {
                    posix = EnumSet.copyOf(posix);
    
                    posix.addAll(toAdd);
                    posix.removeAll(toRemove);
                    return posix;
                }
            };
        }
     
        /**
         * A task that <i>changes</i> a set of {@link PosixFilePermission} elements.
         */
        public interface Changer {
            /**
             * Applies the changes to the given set of permissions.
             *
             * @param   perms
             *          The set of permissions to change
             *
             * @return  The {@code perms} parameter
             */
            Set<PosixFilePermission>  change(Set<PosixFilePermission> set);
        }
     
        /**
         * Changes the permissions of the file using the given Changer.
         * @param attrs 
         */
        static void chmod(Path file, UnifiedFileAttributes attrs, Changer changer) {
            FileUtils.changeFilePermissions(file, attrs , changer.change(attrs.getPermissions()) );
        }
    }

	private boolean bRecurse ;

    public  class ListVisitor implements IPathTreeVisitor {

        private final Changer changer;

        public ListVisitor( Changer changer) {
            this.changer = changer;
        }



        @Override
        public FileVisitResult visitDirectory(Path root, Path directory,
                UnifiedFileAttributes attrs) throws IOException {
             Chmod.chmod(directory, attrs, changer);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path root, Path path,
                UnifiedFileAttributes uattrs) throws IOException {
            Chmod.chmod(path, uattrs, changer);
            return FileVisitResult.CONTINUE ;
        }



        @Override
        public FileVisitResult enterDirectory(Path root, Path directory,
                UnifiedFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }



        @Override
        public FileVisitResult exitDirectory(Path root, Path directory,
                UnifiedFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }

    }

	@Override
	public int run(List<XValue> args) throws Exception {


		Options opts = new Options( "R=recurse");
		opts.parse(args,true); // ignore unknown args 
		args = opts.getRemainingArgs();
		bRecurse = opts.hasOpt("R");
		
		requires(args.size() > 1 ,"Missing arguments");

		String mode = args.remove(0).toString();
	
     // compile the symbolic mode expressions
        Changer changer = Chmod.compile(mode);
        
		for( XValue arg : args){
	          Path path = this.getPath(arg);

		    
		    FileUtils.walkPathTree(path,
		            bRecurse , 
                    new ListVisitor(changer),
                    (new PathMatchOptions() )
                    );

		}



		return 0;

	}

}



//
//
//Copyright (C) 2008-2014 David A. Lee.
//
//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.
//
//The Original Code is: all this file.
//
//The Initial Developer of the Original Code is David A. Lee
//
//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
//Contributor(s): none.
//
