package com.example.commentalert.getreviews.tasks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.example.commentalert.R;
import com.example.commentalert.getreviews.GetReviews;

public class GetCommentListTask extends AsyncTask<GetReviews, Integer, List> {
	ProgressDialog pd;
	private Context context;

	//CommentsListAdapter appleStoreListAdapter;
	View view ;
	
	public GetCommentListTask(Context context,View view) {
		super();
		this.context = context;
		this.view = view;
	}

	
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		pd = new ProgressDialog(context);
		pd.setTitle("Please Wait..");
		pd.setMessage("Loading...");
		pd.setCancelable(false);
		pd.show();
		
		
	}


	@Override
	protected List doInBackground(GetReviews... param) {
		if(param[0] != null){
			try {
				return param[0].exportAllData();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(List result) {
		super.onPostExecute(result);
		pd.dismiss();
		//appleStoreListView = (ExpandableListView)view.findViewById(R.id.comment_list_apple);
		
		/*appleStoreListAdapter = new CommentsListAdapter(context, result);
		appleStoreListView.setAdapter(appleStoreListAdapter);*/
		//TextView timeUpdate = (TextView)view.findViewById(R.id.apple_last_update);
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		//timeUpdate.setText(dateFormat.format(cal.getTime()));
		
		
		
		//view.invalidate();
	}

	
	
	
	
}
