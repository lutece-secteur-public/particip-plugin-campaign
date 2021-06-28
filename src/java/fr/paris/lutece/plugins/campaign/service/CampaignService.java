package fr.paris.lutece.plugins.campaign.service;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import fr.paris.lutece.plugins.campaign.business.Phase;
import fr.paris.lutece.plugins.campaign.business.PhaseHome;
import fr.paris.lutece.plugins.campaign.business.Campaign;
import fr.paris.lutece.plugins.campaign.business.CampaignHome;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

public class CampaignService implements ICampaignService{
	private Map<String, Timestamp> _cache = null;

    private static ICampaignService _singleton;

    public static final String LAST_CAMPAIGN_ID = "campaign.last.id";
    
	public static ICampaignService getInstance( )
    {
		if ( _singleton == null )
        {
            _singleton = new CampaignService( ) ;
        }
        return _singleton;
    }
	
	public void reset( )
    {
        AppLogService.debug( "CampagnePhase cache reset" );

        Map<String, Timestamp> cache = new HashMap<String, Timestamp>( );

        Collection<Phase> phases = PhaseHome.getPhasesList( );
        for ( Phase phase : phases )
        {
            String beginningKey = getKey( phase.getCampaignCode(), phase.getLabel( ), "BEGINNING_DATETIME" );
            String endKey = getKey( phase.getCampaignCode(), phase.getLabel( ), "END_DATETIME" );

            cache.put( beginningKey, phase.getStartingTimeStampDate( ) );
            cache.put( endKey, phase.getEndingTimeStampDate( ) );

            AppLogService.debug(
                    "  -> Added '" + phase.getCampaignCode() + "-" + phase.getLabel( ) + "' = '" + phase.getStartingTimeStampDate( ) + "/" + phase.getEndingTimeStampDate( ) + "'." );
        }

        _cache = cache;
    }
	
	private String getKey( String campain, String phase, String datetimeType )
    {
        return campain + "-" + phase + "-" + datetimeType;
    }
	
	private Map<String, Timestamp> getCache( )
    {
        if ( _cache == null )
            reset( );

        return _cache;
    }
	
	private Timestamp getTimestamp( String campain, String phase, String timestampType )
    {
        String key = getKey( campain, phase, timestampType );
        Timestamp timeStamp = getCache( ).get( key );
        if ( timeStamp == null )
        {
            String errorMsg = "Null datetime for campagne '" + campain + "' and phase '" + phase + "' and timestampType '" + timestampType + "'. ";
            AppLogService.error( errorMsg );
            throw new NoSuchPhaseException( errorMsg );
        }
        return timeStamp;
    }

	public boolean isDuring( String campain, String phase )
    {
        Timestamp beginningTimeStamp = getTimestamp( campain, phase, "BEGINNING_DATETIME" );
        Timestamp endTimeStamp = getTimestamp( campain, phase, "END_DATETIME" );

        Date date = new Date( );

        boolean result = date.after( beginningTimeStamp ) && date.before( endTimeStamp );
        return result;
    }
	
	public boolean isDuring( String phase )
    {
        return isDuring( getLastCampaign( ).getCampaignCode( ), phase );
    }
	
	public boolean isBeforeEnd( String campain, String phase )
    {
        Timestamp timeStamp = getTimestamp( campain, phase, "END_DATETIME" );
        Date date = new Date( );
        boolean result = date.before( timeStamp );
        return result;
    }
	
	public boolean isBeforeEnd( String phase )
    {
        return isBeforeEnd( getLastCampaign( ).getCampaignCode( ), phase );
    }
	
	public boolean isAfterBeginning( String campain, String phase )
    {
        Timestamp timeStamp = getTimestamp( campain, phase, "BEGINNING_DATETIME" );
        Date date = new Date( );
        boolean result = date.after( timeStamp );
        return result;
    }
	
	public boolean isAfterBeginning( String phase )
    {
        return isAfterBeginning( getLastCampaign( ).getCampaignCode(), phase );
    }
	
	public boolean isBeforeBeginning( String campain, String phase )
    {
        Timestamp timeStamp = getTimestamp( campain, phase, "BEGINNING_DATETIME" );
        Date date = new Date( );
        boolean result = date.before( timeStamp );
        return result;
    }
	
	public boolean isBeforeBeginning( String phase )
    {
        return isBeforeBeginning( getLastCampaign( ).getCampaignCode(), phase );
    }
	
	public boolean isAfterEnd( String campain, String phase )
    {
        Timestamp timeStamp = getTimestamp( campain, phase, "END_DATETIME" );
        Date date = new Date( );
        boolean result = date.after( timeStamp );
        return result;
    }

	public boolean isAfterEnd( String phase )
    {
        return isAfterEnd( getLastCampaign( ).getCampaignCode(), phase );
    }

	
	public Campaign getLastCampaign( )
    {
        return CampaignHome.findByPrimaryKey( AppPropertiesService.getPropertyInt( LAST_CAMPAIGN_ID, -1 ) );
    }

}