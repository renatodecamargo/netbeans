/* 
 * File:   DBMSHC.cpp
 * Author: Renato
 * 
 * Created on 31 de Dezembro de 2016, 17:15'
 */
#include "DBMSHM.h"

int DBMSHM::IsIndexName( shm_dbm71_init_record *buff, char * index )
{
	for ( int iIndex = 0; iIndex < DBM_MAX_INDEX; iIndex++ ) {
		if( strcmp(buff->index_name[iIndex], index ) == 0 )
		{
			return (1);
		}
	}
	return(0);
}

int DBMSHM::GetIndexName( char *index_name, shm_dbm71_init_record *buff )
{
	*index_name = 0;
	
	for ( int iIndex = 0; iIndex < DBM_MAX_INDEX; iIndex++ ) {
		if( buff->index_name[iIndex][0] != 0 ) {
			if(( strlen(index_name) + strlen(buff->index_name[iIndex]) + 1 ) < DBM71_SHM_MAX_STRING ) {  // To Prevent a Memory Overflow
				if ( iIndex > 0 )
					strcat(index_name,",");
				strcat(index_name,buff->index_name[iIndex]);
			}
		}
	}
	return(0);
}

shm_header_t *DBMSHM::get_shm ( void ) 
{
	int shmid = shmget( get_shm_IPC_key(), 0, S_IWUSR|S_IRUSR );

	if( shmid == -1 )
		return NULL;		/* Nao foi possivel localizar a shared memory */

	return (shm_header_t *)shmat( shmid, NULL, 0 );
}

shm_header_t *DBMSHM::get_shm_stat ( void ) 
{
	int shmid = shmget( get_shm_IPC_statistics_key(), 0, S_IWUSR|S_IRUSR );

	if( shmid == -1 )
		return NULL;		/* Nao foi possivel localizar a shared memory */

	return (shm_header_t *)shmat( shmid, NULL, 0 );
}

time_t DBMSHM::get_timestamp( void )
{
	return time( NULL );
}

int DBMSHM::compare_timestamp( time_t val1, time_t val2 )
{
	return abs( val1 - val2 );
}

int DBMSHM::show_memory_statistics( void )
{
	int shmid;
	int semid;
	shm_header_t *mem_ptr;
	int all_records = 0;
	struct tm *timeinfo;
	char date_buffer[20] = {0};
		
	//shmid = shmget( get_shm_IPC_statistics_key(), 0, S_IWUSR|S_IRUSR );
	//if( shmid == -1 )
	//	return -1;		/* Nao foi possivel localizar a shared memory */
	//mem_ptr = (shm_header_t *)shmat( shmid, NULL, 0 );
	
	mem_ptr = get_shm_stat();
	
	if( mem_ptr == (void*)-1 )
		return -4;		/* Nao foi possivel anexar a shared memory */
	
	if( mem_ptr->signature != SHM_HEADER_SIGNATURE )
		return -2;		/* A assinatura da shared memory e invalida */

	fprintf(stdout,"============= HEADER INFORMATION ========================================================================================\n");
	fprintf(stdout,"Signature    : %x\n",  mem_ptr->signature);
	fprintf(stdout,"Buffers      : %d\n",  mem_ptr->buff_qty);
	fprintf(stdout,"Buffer Size  : %d\n",  mem_ptr->buff_len);
	fprintf(stdout,"Semaphore ID : %d\n",  mem_ptr->semid);
	fprintf(stdout,"TimeStamp    : %ld\n", mem_ptr->timestamp);
	fprintf(stdout,"Last Index   : %d\n",  mem_ptr->last_index);
	fprintf(stdout,"=========================================================================================================================\n");
	for( int idx = 0; idx < mem_ptr->buff_qty; idx++ )
	{
		shm_dbm71_statistics_record *buff = get_shm_statistics_record( mem_ptr, idx );
		
		if( buff-> signature != SHM_BUFFER_SIGNATURE)
			return -4;      /* Shared Memory Corrompida */
		
		if( buff->command[0] > 0 )
		{
			timeinfo = localtime(&buff->timestamp);
			strftime(date_buffer,sizeof(date_buffer),"%Y.%m.%d %H:%M:%S", timeinfo );
			fprintf(stdout,"#%s FE[%d] Command[%s] PID[%d] SPID[%d] ran in [%f] ms\n",
				date_buffer,
				buff->mbregion,
				buff->command,
				buff->pid,
				buff->spid,
				buff->ellapsedtime);
		}
	}
	fprintf(stdout,"=========================================================================================================================\n");	

	return 0;
}

int DBMSHM::show_memory( char *object_name )
{
	int shmid;
	int semid;
	shm_header_t *mem_ptr;
	int all_records = 0;
	char last_filename[DBM71_SHM_MAX_STRING] = {0};
	char index_name[DBM71_SHM_MAX_STRING] = {0};

	//shmid = shmget( get_shm_IPC_key(), 0, S_IWUSR|S_IRUSR );
	//if( shmid == -1 )
	//	return -1;		/* Nao foi possivel localizar a shared memory */
	//mem_ptr = (shm_header_t *)shmat( shmid, NULL, 0 );
	mem_ptr = get_shm();

	if( mem_ptr == (void*)-1 )
		return -4;		/* Nao foi possivel anexar a shared memory */
	
	if( mem_ptr->signature != SHM_HEADER_SIGNATURE )
		return -2;		/* A assinatura da shared memory e invalida */

	semid = semget( get_shm_IPC_key(), 1, S_IRUSR | S_IWUSR );
	if( semid == -1 )
		return -3;		/* Nao foi possivel localizar o semaforo */
	ASSERT( mem_ptr->semid == semid );
	
	fprintf(stdout,"============= HEADER INFORMATION ========================================================================================\n");
	fprintf(stdout,"Signature    : %x\n",  mem_ptr->signature);
	fprintf(stdout,"Buffers      : %d\n",  mem_ptr->buff_qty);
	fprintf(stdout,"Buffer Size  : %d\n",  mem_ptr->buff_len);
	fprintf(stdout,"Semaphore ID : %d\n",  mem_ptr->semid);
	fprintf(stdout,"TimeStamp    : %ld\n", mem_ptr->timestamp);
	fprintf(stdout,"Last Index   : %d\n",  mem_ptr->last_index);
	
	if(!strcmp(object_name,"all"))
		all_records = 1;
	
	for( int iBufferIndex = 0; iBufferIndex < mem_ptr->buff_qty; iBufferIndex++ )
	{
		shm_dbm71_init_record *buff = get_shm_record( mem_ptr, iBufferIndex );
		
		if( buff-> signature != SHM_BUFFER_SIGNATURE)
			return -4;      /* Shared Memory Corrompida */
		
		if( buff->filename[0] > 0 )
		{
			if( all_records || !(strcmp(object_name,buff->filename)) || !(strcmp(object_name,buff->record_name)) || IsIndexName(buff, object_name))
			{
				if(strcmp(buff->filename,last_filename) != 0)
				   fprintf(stdout,"========================================================================================================================\n");
				GetIndexName( index_name, buff );
				fprintf(stdout,"#%04x->[%s][%s][%s][%2s][%d][%d][%d][%d][%d]\n",
					iBufferIndex, 
					buff->filename,
					buff->record_name,
					index_name,
					buff->fieldname,
					buff->type,
					buff->c_len,
					buff->sybase_len,
					buff->offset_sybase,
					buff->offset_c);
			}
		}
		strcpy(last_filename,buff->filename);
	}
	fprintf(stdout,"========================================================================================================================\n");	

	return 0;
}

int DBMSHM::sem_P( int semid )
{
	static struct sembuf operations[1];

    operations[0].sem_num = 0;
    operations[0].sem_op = -1; /* P operation */
    operations[0].sem_flg = SEM_UNDO;

    return semop( semid, operations, 1 );
}

int DBMSHM::sem_V( int semid )
{
    static struct sembuf operations[1];

    operations[0].sem_num = 0;
    operations[0].sem_op = 1; /* V operation */
    operations[0].sem_flg = SEM_UNDO;

    return semop( semid, operations, 1 );
}

/* Retorna um ponteiro para o buffer apontado por index */
shm_dbm71_init_record *DBMSHM::get_shm_record( shm_header_t *shm, unsigned short index )
{
	shm_dbm71_init_record *ptr_to_first_header;
	size_t total_buff_len;

	ASSERT( shm != NULL );

	ptr_to_first_header = (shm_dbm71_init_record *) ((char*)shm + sizeof(shm_header_t));
	total_buff_len =  shm->buff_len;

	ASSERT( total_buff_len != 0 );

	shm_dbm71_init_record *ret = (shm_dbm71_init_record *)((char*)ptr_to_first_header + total_buff_len * index);

	ASSERT( ret >= ptr_to_first_header );
	ASSERT( ret <  (shm_dbm71_init_record *) ((char*)ptr_to_first_header + total_buff_len * shm->buff_qty) );
	
	#ifdef SHMDEBUG
		printf( "%s() Index:      %u\n", __func__, index );
		printf( "%s() Buffer shm: %p\n", __func__, ret );
	#endif 

	return ret;
}

/* Retorna um ponteiro para o buffer apontado por index */
shm_dbm71_statistics_record * DBMSHM::get_shm_statistics_record( shm_header_t *shm, unsigned short index )
{
	shm_dbm71_statistics_record *ptr_to_first_header;
	size_t total_buff_len;

	ASSERT( shm != NULL );

	ptr_to_first_header = (shm_dbm71_statistics_record *) ((char*)shm + sizeof(shm_header_t));
	total_buff_len =  shm->buff_len;

	ASSERT( total_buff_len != 0 );

	shm_dbm71_statistics_record *ret = (shm_dbm71_statistics_record *)((char*)ptr_to_first_header + total_buff_len * index);

	ASSERT( ret >= ptr_to_first_header );
	ASSERT( ret <  (shm_dbm71_statistics_record *) ((char*)ptr_to_first_header + total_buff_len * shm->buff_qty) );
	
	#ifdef SHMDEBUG
		printf( "%s() Index:      %u\n", __func__, index );
		printf( "%s() Buffer shm: %p\n", __func__, ret );
	#endif 

	return ret;
}

int DBMSHM::get_shm_free_statistics_buffer( shm_header_t *shmstat, unsigned short starting_from, shm_dbm71_statistics_record **buffer )
{
	unsigned short idx;
	int found = FALSE;
	int ret = 0;

	ASSERT( shmstat != NULL );
	
	if( sem_P(shmstat->semid) != 0 )
		return -1; 		/* Nao foi possivel adquirir o semaforo */
		
	ret = shmstat->last_index;
	
	*buffer = get_shm_statistics_record (shmstat, shmstat->last_index );
	
	ret = shmstat->last_index++;
		
	if ( shmstat->last_index > shmstat->buff_qty )
		shmstat->last_index = 0;     // Circular list
			
	if( sem_V(shmstat->semid) != 0 )
		return -2;		/* Nao foi possivel liberar o semaforo */
			
	return ret;
}

int DBMSHM::get_shm_free_buffer( shm_header_t *shm, unsigned short starting_from, shm_dbm71_init_record **buffer )
{
	unsigned short idx;
	int found = FALSE;
	int ret = 0;

	ASSERT( shm != NULL );
	
	if( sem_P(shm->semid) != 0 )
		return -1; 		/* Nao foi possivel adquirir o semaforo */
	
	for( idx = shm->last_index; idx < shm->buff_qty; idx++ )
	{
		shm_dbm71_init_record *buff = get_shm_record( shm, idx );

		if( buff->filename[0] == 0 )
		{
			found = TRUE;
			*buffer = buff;
			break;
		}
	}
	
	if( found == FALSE )
	{
		if( starting_from == 0 )
			ret = -4;	/* Nao ha buffers livres */
		else
		{
			/* Chegamos no final da shared memory */
			shm->last_index = 0;
		
			if( sem_V(shm->semid) != 0 )
				return -2;		/* Nao foi possivel liberar o semaforo */

			return get_shm_free_buffer( shm, 0, buffer );
		}
	}
	else
	{
		shm->last_index = idx;
	}
	
	if( sem_V(shm->semid) != 0 )
		return -3;		/* Nao foi possivel liberar o semaforo */

	return ret;
}

int DBMSHM::update_record( shm_header_t *shm, char *filename, char *index )
{
	ASSERT( shm != NULL );
	
	if( shm->signature != SHM_HEADER_SIGNATURE )
		return -1;		/* A assinatura da shared memory e invalida */

	for( int idx = 0; idx < shm->buff_qty; idx++ )
	{
		shm_dbm71_init_record *buff = get_shm_record( shm, idx );
		
		if( buff-> signature != SHM_BUFFER_SIGNATURE)
			return -2;      /* Shared Memory Corrompida */
			
		if( sem_P(shm->semid) != 0 )
			return -3; 		/* Nao foi possivel adquirir o semaforo */			
		
		if( strcmp( buff->filename, filename) == 0 )
			for ( int iIndex = 0; iIndex < DBM_MAX_INDEX; iIndex++ ) {
				//printf("[%s][%d][%d][%d][%s][%s]\n",buff->fieldname, idx,DBM_MAX_INDEX, iIndex, index, buff->index_name[iIndex] );
				if( buff->index_name[iIndex][0] == 0 ) {
					//printf("[%d][%s]\n",iIndex, index );
					strcpy(buff->index_name[iIndex], index);
					break;
				}
			}

		if( sem_V(shm->semid) != 0 )
			return -4;		/* Nao foi possivel liberar o semaforo */	
	}
	return 0;
}

int DBMSHM::insert_record( shm_header_t *shm, shm_dbm71_init_record *record )
{
	int retcode;
	shm_dbm71_init_record *buff_header = NULL;

	if( shm == NULL )
		return -1;		/* Shared memory nao foi inicializada */

	if( shm->signature != SHM_HEADER_SIGNATURE )
		return -2;		/* A shared memory foi corrompida */

	if( (retcode=get_shm_free_buffer(shm, shm->last_index,&buff_header)) != 0 ) {
		switch( retcode )
		{
			case -1:
				return -3;		/* Nao ha buffers livres */
				break;
		
			case -2:
				return -4;		/* Nao foi possivel liberar o semaforo */
				break;

			case -3:
				return -5;		/* Nao foi possivel adquirir o semaforo */
				break;

			default:
				ASSERT(0);
		}
	}
	
	if( buff_header->signature != SHM_BUFFER_SIGNATURE )
		return -6; 		/* A shared memory foi corrompida */

	if( sem_P(shm->semid) != 0 )
		return -7; 		/* Nao foi possivel adquirir o semaforo */
		
	memcpy( &buff_header->filename, record->filename, sizeof(shm_dbm71_init_record)-sizeof(buff_header->signature));
	
	if( sem_V(shm->semid) != 0 )
		return -8;		/* Nao foi possivel liberar o semaforo */		

	return 0;
}

int DBMSHM::insert_statistics_record( shm_header_t *shmstat, shm_dbm71_statistics_record *record )
{
	int retcode;
	shm_dbm71_statistics_record *buff_header = NULL;
	
	if( shmstat == NULL )
		return -1;		/* Shared memory nao foi inicializada */

	if( shmstat->signature != SHM_HEADER_SIGNATURE )
		return -2;		/* A shared memory foi corrompida */

	if( (retcode=get_shm_free_statistics_buffer(shmstat, shmstat->last_index,&buff_header)) != 0 ) {
		switch( retcode )
		{
	
			case -2:
				return -3;		/* Nao foi possivel liberar o semaforo */
				break;

			case -3:
				return -4;		/* Nao foi possivel adquirir o semaforo */
				break;

			default:
				ASSERT(0);
		}
	}
	
	if( buff_header->signature != SHM_BUFFER_SIGNATURE )
		return -5; 		/* A shared memory foi corrompida */

	if( sem_P(shmstat->semid) != 0 )
		return -6; 		/* Nao foi possivel adquirir o semaforo */	

	memcpy( &buff_header->command, record->command, sizeof(shm_dbm71_statistics_record)-sizeof(buff_header->signature));
	
	if( sem_V(shmstat->semid) != 0 )
		return -7;		/* Nao foi possivel liberar o semaforo */		

	return 0;
}

int DBMSHM::create_semaphore( key_t key )
{
	int retcode;
	
	int semid = semget( key, 1, IPC_CREAT | IPC_EXCL | S_IRUSR | S_IWUSR );
	
	if( semid == -1 )
	{
		if( errno == EEXIST )
		{
			/* O semaforo ja existe, deve ser eliminado */
			semid = semget( key, 1, S_IRUSR | S_IWUSR );
			if( semid == -1 )
				return -1;

			retcode = semctl( semid, 0, IPC_RMID, NULL );
			if( retcode != 0 )
				return -4;
			else
				return create_semaphore( key ); /* tentar cria-lo novamente */
		}
		return -2;
	}

	return semid;
}

int DBMSHM::shm_create_memory( key_t key, size_t size, shm_header_t **shm )
{
	//
	// Create Shared Memory
	//
	int shmid = shmget( key, size, IPC_CREAT | IPC_EXCL | S_IRUSR | S_IWUSR );
	
	if( shmid == -1 )
	{
		if( errno == EEXIST )
		{
			// A shared memory ja existe, deve ser eliminada
			shmid = shmget( key, 1, S_IRUSR | S_IWUSR );
			if( shmid == -1 )
				return -1;

			if( shmctl(shmid, IPC_RMID, 0) == -1 )
				return -2;
			else
				return shm_create_memory( key, size, shm ); /* tentar cria-la novamente */
		}
		return -3;
	}

	/* A shared memory foi criada */
	*shm = (shm_header_t *)shmat( shmid, NULL, 0 );

	memset( *shm, 0, size );

	return 0;
}

void DBMSHM::shm_format_statistics_memory( shm_header_t *shm )
{
	unsigned short iBufferIndex;
	ASSERT( shm != NULL );

	ASSERT( get_shm_statistics_buff_qty() <= 65535 );
	
	shm->signature = SHM_HEADER_SIGNATURE;
	shm->buff_qty = get_shm_statistics_buff_qty();
	shm->buff_len = get_shm_statistics_buff_len();
	shm->timestamp = get_timestamp();
	shm->last_index = 0;

	for( iBufferIndex=0; iBufferIndex < shm->buff_qty; iBufferIndex ++ )
	{
		shm_dbm71_statistics_record *buf = get_shm_statistics_record( shm, iBufferIndex );
		buf->signature = SHM_BUFFER_SIGNATURE;
		buf->mbregion = 0;
        buf->pid = 0;
		buf->spid = 0;
		buf->ellapsedtime = 0;
		buf->timestamp = 0;		
	}
}

void DBMSHM::shm_format_memory( shm_header_t *shm )
{
	unsigned short iBufferIndex;
	
	ASSERT( shm != NULL );

	ASSERT( get_shm_buff_qty() <= 65535 );
	
	shm->signature = SHM_HEADER_SIGNATURE;
	shm->buff_qty = get_shm_buff_qty();
	shm->buff_len = get_shm_buff_len();
	shm->timestamp = get_timestamp();
	shm->last_index = 0;

	for( iBufferIndex=0; iBufferIndex < shm->buff_qty; iBufferIndex ++ )
	{
		shm_dbm71_init_record *buf = get_shm_record( shm, iBufferIndex );
		buf->signature = SHM_BUFFER_SIGNATURE;
		buf->filename[0] = 0;
		buf->record_name[0] = 0;
		for ( int iIndex = 0; iIndex < DBM_MAX_INDEX; iIndex++ ) buf->index_name[iIndex][0] = 0;
		buf->fieldname[0] = 0;		
        buf->type = 0;
		buf->sybase_len = 0;
		buf->offset_sybase = 0;
		buf->offset_c = 0;	
	}
}

int DBMSHM::shm_clear_memory( shm_header_t *shm )
{
	unsigned short iBufferIndex;
	
	ASSERT( shm != NULL );

	ASSERT( get_shm_buff_qty() <= 65535 );	
	
	if( sem_P(shm->semid) != 0 )
		return -7; 		/* Nao foi possivel adquirir o semaforo */
		
	shm->timestamp = get_timestamp();
	shm->last_index = 0;
	
	for( iBufferIndex=0; iBufferIndex < shm->buff_qty; iBufferIndex ++ )
	{
		shm_dbm71_init_record *buf = get_shm_record( shm, iBufferIndex );
		buf->signature = SHM_BUFFER_SIGNATURE;
		buf->filename[0] = 0;
		buf->record_name[0] = 0;
		for ( int iIndex = 0; iIndex < DBM_MAX_INDEX; iIndex++ ) buf->index_name[iIndex][0] = 0;
		buf->fieldname[0] = 0;		
        buf->type = 0;
		buf->sybase_len = 0;
		buf->offset_sybase = 0;
		buf->offset_c = 0;		
	}
	
	if( sem_V(shm->semid) != 0 )
		return -8;		/* Nao foi possivel liberar o semaforo */

	return 0;
}

int DBMSHM::shm_clear_statistics_memory( shm_header_t *shm )
{
	unsigned short iBufferIndex;
	
	ASSERT( shm != NULL );

	ASSERT( get_shm_statistics_buff_qty() <= 65535 );	
	
	if( sem_P(shm->semid) != 0 )
		return -1; 		/* Nao foi possivel adquirir o semaforo */
		
	shm->timestamp = get_timestamp();
	shm->last_index = 0;
	
	for( iBufferIndex=0; iBufferIndex < shm->buff_qty; iBufferIndex ++ )
	{
		shm_dbm71_statistics_record *buf = get_shm_statistics_record( shm, iBufferIndex );
		buf->signature = SHM_BUFFER_SIGNATURE;
		buf->command[0] = 0;
		buf->timestamp = 0;
		buf->mbregion = 0;
		buf->spid = 0;
		buf->pid = 0;
		buf->ellapsedtime = 0;
	}
	
	if( sem_V(shm->semid) != 0 )
		return -2;		/* Nao foi possivel liberar o semaforo */
			
	return 0;
}

size_t DBMSHM::get_shm_statistics_buff_len( void )
{
	static int initialized = 0;
	static size_t value = 0;

	if( !initialized )
		value = sizeof( shm_dbm71_statistics_record  );

	return value;
}

size_t DBMSHM::get_shm_buff_len( void )
{
	static int initialized = 0;
	static size_t value = 0;

	if( !initialized )
	{
		value = sizeof( shm_dbm71_init_record  );
	}

	return value;
}

char * DBMSHM::get_cfg_file( void )
{
	static int initialized = 0;
	static char value[1024] = {0};

	if( !initialized )
	{
		if (cf_openfile(CFG_FILE) < 0) {
			//syslg("Error: unable to open config: %s\n", CFG_FILE);
			fprintf(stdout,"Error: unable to open config: %s\n", CFG_FILE);
			return 0;
		}
		if (cf_locate(PARAM_CFGFILE, value) < 0) {
			//syslg("Error: parameter '%s' not found in file '%s'.\n", PARAM_CFGFILE, CFG_FILE);
			fprintf(stdout,"Error: parameter '%s' not found in file '%s'.\n", PARAM_CFGFILE, CFG_FILE);
			cf_close();
			return 0;
		}
		cf_close();
	}

	return value;
}

int DBMSHM::get_shm_statistics_buff_qty( void )
{
	static int initialized = 0;
	static int value = 0;

	if( !initialized )
	{
		if (cf_openfile(CFG_FILE) < 0) {
			//syslg("Error: unable to open config: %s\n", CFG_FILE);
			fprintf(stdout,"Error: unable to open config: %s\n", CFG_FILE);
			return 0;
		}
		if (cf_locatenum(PARAM_STATISTICSBUFFQTY, &value) < 0) {
			//syslg("Error: parameter '%s' not found in file '%s'.\n",PARAM_BUFFQTY, CFG_FILE);
			fprintf(stdout,"Error: parameter '%s' not found in file '%s'.\n",PARAM_STATISTICSBUFFQTY, CFG_FILE);
			cf_close();
			return 0;
		}
		cf_close();
	}

	return value;
}

int DBMSHM::get_shm_buff_qty( void )
{
	static int initialized = 0;
	static int value = 0;

	if( !initialized )
	{
		if (cf_openfile(CFG_FILE) < 0) {
			//syslg("Error: unable to open config: %s\n", CFG_FILE);
			fprintf(stdout,"Error: unable to open config: %s\n", CFG_FILE);
			return 0;
		}
		if (cf_locatenum(PARAM_BUFFQTY, &value) < 0) {
			//syslg("Error: parameter '%s' not found in file '%s'.\n",PARAM_BUFFQTY, CFG_FILE);
			fprintf(stdout,"Error: parameter '%s' not found in file '%s'.\n",PARAM_BUFFQTY, CFG_FILE);
			cf_close();
			return 0;
		}
		cf_close();
	}

	return value;
}

size_t DBMSHM::get_total_shm_len( void )
{
	size_t len = sizeof( shm_header_t );
	len += get_shm_buff_len() * get_shm_buff_qty();

	return len;
}

size_t DBMSHM::get_total_shm_statistics_len( void )
{
	size_t len = sizeof( shm_header_t );
	len += get_shm_statistics_buff_len() * get_shm_statistics_buff_qty();

	return len;
}

int DBMSHM::init_semaphore( int semid )
{
	int retcode;
	static struct sembuf operations[1];

	/* Inicializar semaforo */
    operations[0].sem_num = 0;
    operations[0].sem_op = 1;
    operations[0].sem_flg = 0;

    retcode = semop( semid, operations, 1 );
	
	if( retcode != 0 )
		return -1;		/* Erro ao tentar liberar o semaforo */

	return 0;
}

key_t DBMSHM::get_shm_IPC_key( void )
{
	register key_t key = DBM71_KEY;

	return key;
}

key_t DBMSHM::get_shm_IPC_statistics_key( void )
{
	register key_t key = DBM71_STATISTICS_KEY;

	return key;
}
