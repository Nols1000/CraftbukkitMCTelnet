package dev.bukkit.Nols1000.MineWire;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.Nols1000.ts.TelnetThread;
import com.github.Nols1000.ts.TelnetUser;
import com.github.Nols1000.ts.event.OnCommandEvent;
import com.sk89q.wepif.PermissionsResolverManager;

public class VirtuellTerminal extends OnCommandEvent implements ConsoleCommandSender {
	
	private JavaPlugin plugin;
	
	private TelnetThread tThread;
	private TelnetUser tUser;
	
	private PermissionsResolverManager perm;
	
	private boolean isInConversation = false;
	private Conversation conversation = null;
	
	private boolean isOp = true;
	
	public VirtuellTerminal(JavaPlugin plugin, TelnetThread thread) {
		
		this.plugin = plugin;
		this.tThread = thread;
		tUser = thread.getUser();
		
		PermissionsResolverManager.initialize(plugin);
		perm = PermissionsResolverManager.getInstance();
	}

	/*
	 * OnCommandEvent start
	 * */
	@Override
	public void onCommand(String cmd){
		
		plugin.getLogger().log(Level.INFO, tUser.getName()+" performed a command: "+cmd);
		
		plugin.getServer().dispatchCommand(this, cmd);
		
    }
	
	@Override
	public void onLoggedIn(){
		
		
	}
	/*
	 * end
	 * */
	
	@Override
	public PermissionAttachment addAttachment(Plugin arg0) {
		
		//return player.addAttachment(arg0);
		return null;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, int arg1) {
		
		//return player.addAttachment(arg0, arg1);
		return null;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1,
			boolean arg2) {
		
		//return player.addAttachment(arg0, arg1, arg2);
		return null;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1,
			boolean arg2, int arg3) {
		
		//return player.addAttachment(arg0, arg1, arg2, arg3);
		return null;
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		
		//return player.getEffectivePermissions();
		return null;
	}

	@Override
	public boolean hasPermission(String arg0) {
		
		return perm.hasPermission(getName(), arg0);
	}

	@Override
	public boolean hasPermission(Permission arg0) {
		
		String arg1 = arg0.getName();
		
		return perm.hasPermission(getName(), arg1);
	}

	@Override
	public boolean isPermissionSet(String arg0) {
		
		return true;
	}

	@Override
	public boolean isPermissionSet(Permission arg0) {
		
		return true;
	}

	@Override
	public void recalculatePermissions() {
		
		
	}

	@Override
	public void removeAttachment(PermissionAttachment arg0) {
		

	}

	@Override
	public boolean isOp() {
		
		return isOp;
	}

	@Override
	public void setOp(boolean arg0) {
		
		isOp = arg0;
	}

	@Override
	public String getName() {
		
		return tUser.getName();
	}

	@Override
	public Server getServer() {
		
		return plugin.getServer();
	}

	@Override
	public void sendMessage(String arg0) {
		
		try {
			
			tThread.sendMsg(ChatColor.stripColor(arg0)+"\n\r");
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

	@Override
	public void sendMessage(String[] arg0) {
		
		String[] arg1 = new String[arg0.length];
		
		for(int i = 0; i < arg0.length; i++){
			
			arg1[i] = ChatColor.stripColor(arg0[i])+"\n\r";
		}
		
		try {
			
			tThread.sendMsg(arg1);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

	@Override
	public void abandonConversation(Conversation arg0) {
		
		if(conversation != null){
			
			isInConversation = false;
			arg0.abandon();
		}
	}

	@Override
	public void abandonConversation(Conversation arg0,
			ConversationAbandonedEvent arg1) {
		
		if(conversation != null){
			
			isInConversation = false;
			arg0.abandon(arg1);
		}
	}

	@Override
	public void acceptConversationInput(String arg0) {
		
		if(conversation != null){
			
			conversation.acceptInput(arg0);
		}
	}

	@Override
	public boolean beginConversation(Conversation arg0) {
		
		isInConversation = true;
		conversation = arg0;
		arg0.begin();
		
		return false;
	}

	@Override
	public boolean isConversing() {
		
		return isInConversation;
	}

	@Override
	public void sendRawMessage(String arg0) {
		
		try {
			
			tThread.sendMsg(arg0);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}	
}
