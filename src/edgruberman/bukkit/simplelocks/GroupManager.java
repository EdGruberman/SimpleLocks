package edgruberman.bukkit.simplelocks;

import java.util.ArrayList;
import java.util.List;

import edgruberman.bukkit.simplegroups.Group;
import edgruberman.bukkit.simplegroups.Member;

/**
 * Group management abstraction.
 * TODO: a lot.
 */
public class GroupManager {
    
    protected static List<String> getMembers(String name) {
        return GroupManager.getMembers(name, false);
    }
    
    protected static List<String> getMembers(String name, boolean expand) {
        List<String> members = GroupManager.getMembersSource(name);
        if (!expand) return members;
        
        if (members == null) return null;

        //TODO Abstract expansion for wrapping external plugin;
        //TODO Expand recursively more than one level, but check for circular references along the way.
        //TODO Use a constant that describes group format for regex matching.
        List<String> expanded = new ArrayList<String>();
        for (String member : members) {
            if (member.startsWith("[") && member.endsWith("]")) {
                // Expand group name
                List<String> submembers = GroupManager.getMembers(member.substring(1, member.length() - 1));
                if (submembers != null)
                    expanded.addAll(submembers);
            } else {
                // Direct player name
                expanded.add(member);
            }
        }
        return expanded;
    }
    
    private static List<String> getMembersSource(String name) {
        List<String> members = new ArrayList<String>();

        Group group = edgruberman.bukkit.simplegroups.GroupManager.getGroup(name);
        if (group == null) return null;
        
        for (Member member : group.getMembers()) {
            members.add(member.getName());
        }
        
        return members;
    }
    
    protected static boolean isMember(String group, String player) {
        List<String> members = GroupManager.getMembers(group, true);
        
        if (members == null) return false;
        
        for (String member : members) {
            if (member.equalsIgnoreCase(player)) return true;
        }
        
        return false;
    }
}