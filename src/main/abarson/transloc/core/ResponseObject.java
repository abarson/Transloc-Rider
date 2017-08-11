package abarson.transloc.core;

public class ResponseObject {
	private String ssmlSpeech;
	private String textSpeech;
	private String cardTitle;
	private String cardText;
	
	public ResponseObject(String ssmlSpeech, String textSpeech, String cardTitle, String cardText){
		setSsmlSpeech(ssmlSpeech);
		setTextSpeech(textSpeech);
		setCardTitle(cardTitle);
		setCardText(cardText);
	}

	public String getSsmlSpeech() {
		return ssmlSpeech;
	}

	public void setSsmlSpeech(String ssmlSpeech) {
		this.ssmlSpeech = ssmlSpeech;
	}

	public String getTextSpeech() {
		return textSpeech;
	}

	public void setTextSpeech(String textSpeech) {
		this.textSpeech = textSpeech;
	}

	public String getCardTitle() {
		return cardTitle;
	}

	public void setCardTitle(String cardTitle) {
		this.cardTitle = cardTitle;
	}

	public String getCardText() {
		return cardText;
	}

	public void setCardText(String cardText) {
		this.cardText = cardText;
	}
}
