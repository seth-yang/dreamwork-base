package org.dreamwork.persistence.test;

import org.dreamwork.telnet.*;
import org.dreamwork.telnet.command.Command;
import org.dreamwork.telnet.command.Exit;
import org.dreamwork.telnet.command.Help;
import org.dreamwork.telnet.command.Quit;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by seth.yang on 2018/9/20
 */
public class ConsoleTest {
    private static final class TestCommand extends Command {
        public TestCommand (String name, String alias, String desc) {
            super (name, alias, desc);
        }

        @Override
        public void parse (String line) {

        }

        @Override
        public boolean isOptionPresent (String name) {
            return false;
        }

        /**
         * 执行命令
         *
         * @param console 当前控制台
         * @throws IOException io exception
         */
        @Override
        public void perform (Console console) throws IOException {
            console.println (name);
        }

        /**
         * 根据输入的文本猜测可能合法的后续输入.
         * <ul>
         * <li>如果猜测无结果，返回 null</li>
         * <li>如果能够确定匹配后续输入，返回一条确切记录</li>
         * <li>如果能够猜测出多条可能的输入，返回一个列表</li>
         * </ul>
         *
         * @param text 输入的文本
         * @return 可能合法的后续输入.
         */
        @Override
        public List<String> guess (String text) {
            return null;
        }

        @Override
        public void showHelp (Console console) {
        }
    }

    static boolean running = true;
    public static void main (String[] args) throws IOException {
        ExecutorService executor = Executors.newCachedThreadPool ();
        ServerSocket server = new ServerSocket (1088);
        while (running) {
            final Socket socket = server.accept ();
            executor.execute (new Runnable () {
                @Override
                public void run () {
                    try {
                        ConnectionData data = new ConnectionData ();
                        Console console = new Console (socket.getInputStream (), socket.getOutputStream (), data);

                        SimpleCommandParser parser = new SimpleCommandParser (true);
//                        parser.registerCommand (new Help (parser));
                        parser.registerCommand (new TestCommand ("test", null, "raise a test"));
                        parser.registerCommand (new TestCommand ("test-command", "tc", "another test command"));
                        parser.registerCommand (new TestCommand ("shot", "s", "this command has a very very big description. " +
                                "it may be wrap the line. Lauded among the most successful influencers in Open Source, The " +
                                "Apache Software Foundation's commitment to collaborative development has long served " +
                                "as a model for producing consistently high quality software that advances the future " +
                                "of open development"));
/*
                        parser.registerCommand (new Quit ());
                        parser.registerCommand (new Exit ());
*/
                        parser.registerCommand (new TestCommand ("first", null, null));
                        parser.registerCommand (new CreateCommand ());
//                        parser.registerCommand (SimpleCommandParser.BASE_COMMANDS);

                        console.setCommandParser (parser);
                        console.setPrompt ("Simple Console");
                        console.setForegroundColor (TerminalIO.YELLOW);
                        console.println ("type 'h' or 'help' for helping");
                        console.println ();
                        console.setForegroundColor (TerminalIO.COLORINIT);

                        console.loop ();
                    } catch (IOException ex) {
                        ex.printStackTrace ();
                    } finally {
                        try {
                            socket.close ();
                        } catch (IOException e) {
                            e.printStackTrace ();
                        }
                    }
                }
            });
        }
    }
}
