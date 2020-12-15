package DO;

public class Room {
	//방의 ID
	private int rID;
	//방에 있는 유저 수
	private int userCount;
	//방을 만든 사람
	private String masterName;
	//방에 입장한 유저의 ID 목록
	public String roomInUserList;

	public Room() {
		this.rID = 0;
		this.userCount = 0;
		this.masterName = "";
		this.roomInUserList = "";
	}

	public Room(int rID, int userCount, String masterName, String roomInUserList) {
		this.rID = rID;
		this.userCount = userCount;
		this.masterName = masterName;
		this.roomInUserList = roomInUserList;
	}

	public int getrID() {
		return rID;
	}

	public void setrID(int rID) {
		this.rID = rID;
	}

	public int getUserCount() {
		return userCount;
	}

	public void setUserCount(int userCount) {
		this.userCount = userCount;
	}

	public String getMasterName() {
		return masterName;
	}

	public void setMasterName(String masterName) {
		this.masterName = masterName;
	}

	public String getRoomInUserList() {
		return roomInUserList;
	}

	public void setRoomInUserList(String roomInUserList) {
		this.roomInUserList = roomInUserList;
	}

	@Override
	public String toString() {
		return "Room [rID=" + rID + ", userCount=" + userCount + ", masterName=" + masterName + ", roomUserList=" + roomInUserList +"]";
	}
}
