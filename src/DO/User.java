package DO;

public class User {
	//유저의 고유번호
	private int pryNumber;
	//유저의 ID
	private String idName;
	//유저의 비밀번호
	private String password;
	//유저의 별명
	private String nickName;
	//유저의 이름
	private String name;
	//유저의 이메일
	private String email;
	//유저의 나이
	private String age;
	//유저의 오늘의 한마디
	private String today_line;
	//유저의 접속 상태
	private int state;
	
	
	public User() {
		this(0, "", "", "", "", "", "", "Enter Today Message", 0);
	}

	public User(int pryNumber, String idName, String password, String nickName, String name, String email ,String age,
			String today_line, int state) {
		super();
		this.pryNumber = pryNumber;
		this.idName = idName;
		this.password = password;
		this.name = name;
		this.age = age;
		this.email = email;
		this.today_line = today_line;
		this.state = state;
	}

	public int getPryNumber() {
		return pryNumber;
	}

	public void setPryNumber(int pryNumber) {
		this.pryNumber = pryNumber;
	}

	public String getIdName() {
		return idName;
	}

	public void setIdName(String idName) {
		this.idName = idName;
	}
	
	public String getnickName() {
		return nickName;
	}

	public void setnickName(String nickName) {
		this.nickName = nickName;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getToday_line() {
		return today_line;
	}

	public void setToday_line(String today_line) {
		this.today_line = today_line;
	}
	
	public int getState() {
		return state;
	}
	
	public void setState(int state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "User [pryNumber=" + pryNumber + ", idName=" + idName + ", password=" + password + ", nickName=" + nickName
				+ ", name=" + name + ", email=" + email + ", age=" + age + ", today_line=" + today_line +  ", state=" + state + "]";
	}
}
