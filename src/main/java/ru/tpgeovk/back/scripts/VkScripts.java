package ru.tpgeovk.back.scripts;

public class VkScripts {

    public static final String SCRIPT_EVENTS = "var events = API.groups.search({\"q\":\"*\",\"cityId\":%d,\"count\":25,\"type\":\"event\",\"future\":true,});\n" +
            "var eventIds = events.items@.id;\nreturn API.groups.getById({\"group_ids\":eventIds,\"fields\":\"description,members_count,place\"});\n";

    public static final String PLACE_CHECKINS_USERS = "var placeId = %d;\n" +
            "var checkins = API.places.getCheckins({\"place\":placeId});\n" +
            "var users = checkins.items@.user_id;\n" +
            "var total = checkins.count;\n" +
            "if (total <= 20) { return users; }\n" +
            "var offset = 20;\n" +
            "while ((offset < (total-20)) && (offset <= 400)) {\n" +
            "offset = offset + 20;\n" +
            "users = users + API.places.getCheckins({\"place\":placeId,\"offset\":offset}).items@.user_id;\n" +
            "}\n" +
            "return users;";

    public static final String COORD_CHECKINS_USERS = "var lat = %f;\nvar lon = %f;\n" +
            "var checkins = API.places.getCheckins({\"latitude\":lat,\"longitude\":lon});\n" +
            "var users = checkins.items@.user_id;\n" +
            "var total = checkins.count;\n" +
            "if (total <= 20) { return users; }\n" +
            "var offset = 20;\n" +
            "while ((offset < (total-20)) && (offset <= 400)) {\n" +
            "offset = offset + 20;\n" +
            "users = users + API.places.getCheckins({\"latitude\":lat,\"longitude\":lon,\"offset\":offset}).items@.user_id;\n" +
            "}\n" +
            "return users;";

    public static final String GROUP_MEMBERS = "var userId = %d;\nvar groupsOffset = %d;\nvar users = %s;\n" +
            "var groups = API.groups.get({\"user_id\":userId,\"offset\":groupsOffset,\"count\":24}).items;\n" +
            "if (groups.length == 0) { return []; }\n" +
            "var i = 0;\n" +
            "var res = [];\n" +
            "var group;\n" +
            "while (i < groups.length) {\n" +
            "group = groups[i];\n" +
            "res = res + [{\"groupId\": group,\"members\": API.groups.isMember({\"group_id\": group,\"user_ids\": users})}];\n" +
            "i = i + 1;\n" +
            "}\nreturn res;";

    public static final String GROUPS_SEARCH = "var group = \"%s\";\n" +
            "var groupIds = [];\n" +
            "var found = API.groups.search({\"q\":group,\"future\":true});\n" +
            "if (found.count == 0) { return []; }\n" +
            "groupIds = groupIds + [found.items[0].id];\n" +
            "if (found.count > 1) { groupIds = groupIds + [found.items[1].id]; }\n" +
            "if (found.count > 2) { groupIds = groupIds + [found.items[2].id]; }\n" +
            "var groupsFull = API.groups.getById({\"group_ids\": groupIds, \"fields\":\"description,members_count,place\"});\n" +
            "return groupsFull;";

    public static final String CREATE_CHECKIN = "var userId = %d;\nvar placeId = %d;\n" +
            "return API.places.checkin({\"place_id\": placeId});";
    public static final String CREATE_CHECKIN_TEXT = "var userId = %d;\nvar placeId = %d;\nvar text = \"%s\";\n" +
            "return API.places.checkin({\"place_id\": placeId, \"text\": text});";

    public static final String GET_USER_FEATURES = "var userId = %d;\n" +
            "var groups = API.groups.get({\"user_id\":userId,\"count\":1000});\n" +
            "var user = API.users.get({\"user_ids\":userId,\"fields\":\"bdate,sex\"})[0];\n" +
            "return {\"userId\":user.id, \"bdate\":user.bdate, \"gender\":user.sex, \"groups\":groups.items};\n";

    public static final String GET_USERS_FEATURES = "var userIds = %s;\n" +
            "var result = [];\n" +
            "var i = 0;\n" +
            "var users = API.users.get({\"user_ids\":userIds,\"fields\":\"bdate,sex\"});\n" +
            "while (i < userIds.length) {\n" +
            "var groups = API.groups.get({\"user_id\":userIds[i],\"count\":1000});\n" +
            "var user = users[i];\n" +
            "result = result + [{\"userId\":user.id, \"bdate\":user.bdate, \"gender\":user.sex, \"groups\":groups.items}];\n" +
            "i = i + 1;\n" +
            "}\n" +
            "return result;\n";

    public static final String GET_USERS_GROUPS = "var userIds = %s;\n" +
            "var result = [];\n" +
            "var i = 0;\n" +
            "while (i < userIds.length) {\n" +
            "var groups = API.groups.get({\"user_id\":userIds[i],\"count\":70});\n" +
            "if (groups) {\n" +
            "result = result + groups.items;\n" +
            "}\n" +
            "i = i + 1;\n" +
            "}\n" +
            "return result;\n";

    public static final String GET_MUTUAL_FRIENDS = "var userIds = %s;\n" +
            "return API.friends.getMutual({\"target_uids\": userIds});\n";


    public static final String FILTER_DEACTIVATED_USERS = "var userIds = %s;\n" +
            "var users = API.users.get({\"user_ids\": userIds});\n" +
            "return users@.deactivated;";
}
